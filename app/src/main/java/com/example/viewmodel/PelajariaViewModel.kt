package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.data.PelajariaStorage
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

enum class AppRootDestination {
    SPLASH,
    AUTH,
    MAIN
}

sealed interface SubRoute {
    data object AddDocument : SubRoute
    data class TeacherGradingDetail(val documentId: String, val studentId: String) : SubRoute
    data class StudentLearning(val documentId: String) : SubRoute
    data class TeacherDocumentPreview(val documentId: String) : SubRoute
}

data class PelajariaUiState(
    val root: AppRootDestination = AppRootDestination.SPLASH,
    val session: UserSession? = null,
    val appState: PelajariaAppState = PelajariaAppState(),
    val selectedTab: Int = 0,
    val subRouteStack: List<SubRoute> = emptyList(),
    val selectedParentStudentId: String? = null,
    val feedbackMessage: String? = null
)

class PelajariaViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = PelajariaStorage(application.applicationContext)

    private val _uiState = MutableStateFlow(
        PelajariaUiState(
            root = AppRootDestination.SPLASH,
            session = storage.loadSession(),
            appState = storage.loadState()
        )
    )
    val uiState: StateFlow<PelajariaUiState> = _uiState.asStateFlow()

    init {
        val loaded = _uiState.value
        val firstChild = loaded.appState.students.firstOrNull {
            it.parentId == loaded.session?.userId
        }?.id ?: loaded.appState.students.firstOrNull()?.id
        _uiState.update { it.copy(selectedParentStudentId = firstChild) }
    }

    private fun updateAndPersist(transform: (PelajariaAppState) -> PelajariaAppState) {
        _uiState.update { current ->
            val updated = transform(current.appState)
            storage.saveState(updated)
            current.copy(appState = updated)
        }
    }

    fun showFeedback(message: String) {
        _uiState.update { it.copy(feedbackMessage = message) }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedbackMessage = null) }
    }

    fun startFromSplash() {
        val session = _uiState.value.session
        val valid = session != null && isSessionValid(session, _uiState.value.appState)
        _uiState.update {
            it.copy(
                root = if (valid) AppRootDestination.MAIN else AppRootDestination.AUTH,
                selectedTab = 0,
                subRouteStack = emptyList()
            )
        }
    }

    private fun isSessionValid(session: UserSession, state: PelajariaAppState): Boolean {
        return when (session.role) {
            UserRole.TEACHER -> state.teachers.any { it.id == session.userId }
            UserRole.STUDENT -> state.students.any { it.id == session.userId }
            UserRole.PARENT -> state.parents.any { it.id == session.userId }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index, subRouteStack = emptyList()) }
    }

    fun navigateTo(route: SubRoute) {
        _uiState.update { it.copy(subRouteStack = it.subRouteStack + route) }
    }

    fun popSubRoute() {
        _uiState.update {
            if (it.subRouteStack.isNotEmpty()) it.copy(subRouteStack = it.subRouteStack.dropLast(1)) else it
        }
    }

    fun selectParentStudent(studentId: String) {
        _uiState.update { it.copy(selectedParentStudentId = studentId) }
    }

    // --- File helper for Camera Bitmap / Persistent Media ---
    fun saveBitmapToAppStorage(bitmap: Bitmap, prefix: String = "pelajaria_cam"): String {
        val ctx = getApplication<Application>().applicationContext
        val file = File(ctx.filesDir, "${prefix}_${UUID.randomUUID().toString().take(8)}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return Uri.fromFile(file).toString()
    }

    // --- Authentication ---
    fun loginWithEmail(role: UserRole, email: String, password: String): String? {
        val state = _uiState.value.appState
        val cleanEmail = email.trim().lowercase()
        val cleanPass = password.trim()
        if (cleanEmail.isBlank() || cleanPass.isBlank()) return "Email dan kata sandi wajib diisi."
        return when (role) {
            UserRole.TEACHER -> {
                val teacher = state.teachers.firstOrNull {
                    it.email.lowercase() == cleanEmail && it.password == cleanPass
                } ?: return "Email atau kata sandi Guru tidak cocok."
                activateSession(UserSession(UserRole.TEACHER, teacher.id), "Selamat datang, ${teacher.name}!")
                null
            }
            UserRole.PARENT -> {
                val parent = state.parents.firstOrNull {
                    it.email.lowercase() == cleanEmail && it.password == cleanPass
                } ?: return "Email atau kata sandi Orang Tua tidak cocok."
                activateSession(UserSession(UserRole.PARENT, parent.id), "Selamat datang, ${parent.name}!")
                null
            }
            UserRole.STUDENT -> "Pilih profil murid untuk masuk."
        }
    }

    fun loginAsDemo(role: UserRole, userId: String) {
        val label = when (role) {
            UserRole.TEACHER -> _uiState.value.appState.teachers.find { it.id == userId }?.name ?: "Guru"
            UserRole.STUDENT -> _uiState.value.appState.students.find { it.id == userId }?.name ?: "Murid"
            UserRole.PARENT -> _uiState.value.appState.parents.find { it.id == userId }?.name ?: "Orang Tua"
        }
        activateSession(UserSession(role, userId), "Masuk sebagai $label")
    }

    fun registerTeacher(name: String, email: String, password: String, subject: String): String? {
        if (name.isBlank() || !email.contains("@") || password.length < 4) {
            return "Lengkapi nama, email valid, dan kata sandi minimal 4 karakter."
        }
        if (_uiState.value.appState.teachers.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return "Email guru sudah terdaftar."
        }
        val created = TeacherAccount(
            id = "teacher_${UUID.randomUUID().toString().take(6)}",
            name = name.trim(),
            email = email.trim(),
            password = password.trim(),
            subject = subject.ifBlank { "Tematik" }
        )
        updateAndPersist { it.copy(teachers = it.teachers + created) }
        activateSession(UserSession(UserRole.TEACHER, created.id), "Akun Guru berhasil dibuat!")
        return null
    }

    fun registerParent(name: String, email: String, password: String): String? {
        if (name.isBlank() || !email.contains("@") || password.length < 4) {
            return "Lengkapi nama, email valid, dan kata sandi minimal 4 karakter."
        }
        if (_uiState.value.appState.parents.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return "Email orang tua sudah terdaftar."
        }
        val created = ParentAccount(
            id = "parent_${UUID.randomUUID().toString().take(6)}",
            name = name.trim(),
            email = email.trim(),
            password = password.trim()
        )
        updateAndPersist { it.copy(parents = it.parents + created) }
        activateSession(UserSession(UserRole.PARENT, created.id), "Akun Orang Tua berhasil dibuat!")
        return null
    }

    private fun activateSession(session: UserSession, greeting: String) {
        storage.saveSession(session)
        val defaultChild = if (session.role == UserRole.PARENT) {
            _uiState.value.appState.students.firstOrNull { it.parentId == session.userId }?.id
                ?: _uiState.value.appState.students.firstOrNull()?.id
        } else {
            _uiState.value.selectedParentStudentId
        }
        _uiState.update {
            it.copy(
                session = session,
                root = AppRootDestination.MAIN,
                selectedTab = 0,
                subRouteStack = emptyList(),
                selectedParentStudentId = defaultChild,
                feedbackMessage = greeting
            )
        }
    }

    fun logout() {
        storage.saveSession(null)
        _uiState.update {
            it.copy(
                session = null,
                root = AppRootDestination.AUTH,
                selectedTab = 0,
                subRouteStack = emptyList()
            )
        }
    }

    fun resetDemoData() {
        val reset = storage.resetToDemo()
        storage.saveSession(null)
        _uiState.update {
            PelajariaUiState(
                root = AppRootDestination.AUTH,
                session = null,
                appState = reset,
                selectedParentStudentId = reset.students.firstOrNull()?.id,
                feedbackMessage = "Data simulasi demo berhasil dipulihkan."
            )
        }
    }

    // --- Teacher: Publish Document (Original / Interactive / Digital Questions) ---
    fun publishTeacherDocument(document: DocumentItem) {
        updateAndPersist { state ->
            state.copy(documents = listOf(document) + state.documents)
        }
        showFeedback("\"${document.title}\" berhasil dibagikan kepada murid!")
    }

    // --- Teacher: Manual Grading ---
    fun saveManualGrade(
        documentId: String,
        studentId: String,
        score: Int,
        teacherNote: String
    ) {
        val cleanScore = score.coerceIn(0, 100)
        updateAndPersist { state ->
            val doc = state.documents.find { it.id == documentId } ?: return@updateAndPersist state
            val existing = doc.submissions[studentId] ?: StudentSubmission(
                documentId = documentId,
                studentId = studentId,
                status = SubmissionStatus.WAITING_GRADE
            )
            val graded = existing.copy(
                status = SubmissionStatus.GRADED,
                score = cleanScore,
                teacherNote = teacherNote.trim(),
                gradedAtLabel = "Baru saja"
            )
            val updatedDocs = state.documents.map { item ->
                if (item.id == documentId) item.copy(submissions = item.submissions + (studentId to graded)) else item
            }
            val newRecord = ActivityRecord(
                id = "act_${UUID.randomUUID().toString().take(6)}",
                studentId = studentId,
                title = "${doc.title} dinilai ${doc.teacherName}",
                subject = doc.subject,
                category = "Nilai",
                scoreLabel = "Nilai $cleanScore",
                timestampLabel = "Baru saja"
            )
            state.copy(
                documents = updatedDocs,
                activityRecords = listOf(newRecord) + state.activityRecords
            )
        }
        showFeedback("Penilaian manual berhasil disimpan!")
    }

    // --- Student: Mark Material Read & Submit Work ---
    fun markMaterialAsRead(documentId: String, studentId: String) {
        updateAndPersist { state ->
            val doc = state.documents.find { it.id == documentId } ?: return@updateAndPersist state
            if (doc.readByStudentIds.contains(studentId)) return@updateAndPersist state
            val updatedDocs = state.documents.map { item ->
                if (item.id == documentId) item.copy(readByStudentIds = item.readByStudentIds + studentId) else item
            }
            val newRecord = ActivityRecord(
                id = "act_${UUID.randomUUID().toString().take(6)}",
                studentId = studentId,
                title = "Membaca materi ${doc.title}",
                subject = doc.subject,
                category = "Materi",
                scoreLabel = "Sudah Dibaca",
                timestampLabel = "Baru saja"
            )
            state.copy(
                documents = updatedDocs,
                activityRecords = listOf(newRecord) + state.activityRecords
            )
        }
        showFeedback("Bagus! Materi ditandai sudah dibaca.")
    }

    fun submitStudentWork(
        documentId: String,
        studentId: String,
        textAnswer: String,
        photoUriString: String,
        sampleAnswerPhotoKey: String,
        digitalAnswers: Map<String, String>
    ) {
        updateAndPersist { state ->
            val doc = state.documents.find { it.id == documentId } ?: return@updateAndPersist state
            val sub = StudentSubmission(
                documentId = documentId,
                studentId = studentId,
                status = SubmissionStatus.WAITING_GRADE,
                textAnswer = textAnswer.trim(),
                photoUriString = photoUriString,
                sampleAnswerPhotoKey = sampleAnswerPhotoKey,
                digitalAnswers = digitalAnswers,
                submittedAtLabel = "Baru saja"
            )
            val updatedDocs = state.documents.map { item ->
                if (item.id == documentId) item.copy(submissions = item.submissions + (studentId to sub)) else item
            }
            val newRecord = ActivityRecord(
                id = "act_${UUID.randomUUID().toString().take(6)}",
                studentId = studentId,
                title = "Mengirim jawaban ${doc.title}",
                subject = doc.subject,
                category = "Tugas",
                scoreLabel = "Menunggu Nilai",
                timestampLabel = "Baru saja"
            )
            state.copy(
                documents = updatedDocs,
                activityRecords = listOf(newRecord) + state.activityRecords
            )
        }
        showFeedback("Jawaban berhasil dikirim! Menunggu penilaian guru.")
    }

    // --- Parent: Child Profile Management (Monitor-only for learning content) ---
    fun saveStudentProfileByParent(
        existingId: String?,
        name: String,
        level: EducationLevel,
        grade: String,
        emoji: String,
        colorHex: Long
    ): String? {
        val parentId = _uiState.value.session?.userId ?: return "Sesi tidak valid."
        if (name.isBlank()) return "Nama anak wajib diisi."
        updateAndPersist { state ->
            if (existingId == null) {
                val created = StudentProfile(
                    id = "student_${UUID.randomUUID().toString().take(6)}",
                    parentId = parentId,
                    name = name.trim(),
                    level = level,
                    grade = grade,
                    avatarEmoji = emoji,
                    avatarColorHex = colorHex
                )
                state.copy(students = state.students + created)
            } else {
                state.copy(
                    students = state.students.map {
                        if (it.id == existingId) {
                            it.copy(name = name.trim(), level = level, grade = grade, avatarEmoji = emoji, avatarColorHex = colorHex)
                        } else it
                    }
                )
            }
        }
        showFeedback(if (existingId == null) "Profil anak ditambahkan." else "Profil anak diperbarui.")
        return null
    }

    // --- Query Helpers ---
    fun studentDocuments(studentId: String): List<DocumentItem> {
        return _uiState.value.appState.documents.filter { it.targetStudentIds.contains(studentId) }
    }

    fun studentMaterials(studentId: String): List<DocumentItem> {
        return studentDocuments(studentId).filter { !it.isAssignment }
    }

    fun studentAssignments(studentId: String): List<DocumentItem> {
        return studentDocuments(studentId).filter { it.isAssignment }
    }

    fun studentAverageScore(studentId: String): Int {
        val scores = studentAssignments(studentId).mapNotNull { it.submissions[studentId]?.score }
        return if (scores.isEmpty()) 0 else scores.average().toInt()
    }
}
