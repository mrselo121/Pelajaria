package com.example.model

enum class UserRole(val label: String) {
    TEACHER("Guru"),
    STUDENT("Murid"),
    PARENT("Orang Tua")
}

enum class EducationLevel(val label: String) {
    TK("TK"),
    SD("SD")
}

enum class DocumentFormat(val label: String) {
    IMAGE("Foto / Gambar"),
    PDF("Dokumen PDF")
}

enum class PresentationMode(val label: String) {
    ORIGINAL_MATERIAL("Dokumen Asli • Materi Bacaan"),
    ORIGINAL_WORKSHEET("Dokumen Asli • Soal / Lembar Kerja"),
    INTERACTIVE_MATERIAL("Materi Interaktif"),
    DIGITAL_QUESTIONS("Soal Digital")
}

enum class SubmissionStatus(val label: String) {
    NOT_STARTED("Belum Dikerjakan"),
    WAITING_GRADE("Menunggu Penilaian"),
    GRADED("Sudah Dinilai")
}

data class TeacherAccount(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
    val subject: String,
    val classLabel: String = "Kelas 2 SD"
)

data class StudentProfile(
    val id: String,
    val parentId: String,
    val name: String,
    val level: EducationLevel,
    val grade: String,
    val avatarEmoji: String,
    val avatarColorHex: Long
)

data class ParentAccount(
    val id: String,
    val name: String,
    val email: String,
    val password: String
)

data class InteractiveSection(
    val id: String,
    val title: String,
    val explanation: String,
    val simpleExample: String,
    val touchPrompt: String,
    val touchReveal: String,
    val visualEmoji: String
)

data class DigitalQuestion(
    val id: String,
    val prompt: String,
    val isEssay: Boolean = false,
    val options: List<String> = emptyList(),
    val sourceNote: String = ""
)

data class StudentSubmission(
    val documentId: String,
    val studentId: String,
    val status: SubmissionStatus,
    val textAnswer: String = "",
    val photoUriString: String = "",
    val sampleAnswerPhotoKey: String = "",
    val digitalAnswers: Map<String, String> = emptyMap(),
    val submittedAtLabel: String = "",
    val score: Int? = null,
    val teacherNote: String = "",
    val gradedAtLabel: String = ""
)

data class DocumentItem(
    val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val level: EducationLevel,
    val grade: String,
    val format: DocumentFormat,
    val presentationMode: PresentationMode,
    val teacherId: String,
    val teacherName: String,
    val targetStudentIds: List<String>,
    val dueDate: String = "",
    val sourceUriString: String = "",
    val samplePresetKey: String = "math_worksheet",
    val pdfSimulatedPages: List<String> = emptyList(),
    val interactiveSections: List<InteractiveSection> = emptyList(),
    val digitalQuestions: List<DigitalQuestion> = emptyList(),
    val hasOcrWarning: Boolean = false,
    val ocrWarningText: String = "",
    val readByStudentIds: List<String> = emptyList(),
    val submissions: Map<String, StudentSubmission> = emptyMap(),
    val createdAtLabel: String = "Hari ini"
) {
    val isAssignment: Boolean
        get() = presentationMode == PresentationMode.ORIGINAL_WORKSHEET ||
            presentationMode == PresentationMode.DIGITAL_QUESTIONS
}

data class ActivityRecord(
    val id: String,
    val studentId: String,
    val title: String,
    val subject: String,
    val category: String,
    val scoreLabel: String,
    val timestampLabel: String
)

data class PelajariaAppState(
    val teachers: List<TeacherAccount> = emptyList(),
    val students: List<StudentProfile> = emptyList(),
    val parents: List<ParentAccount> = emptyList(),
    val documents: List<DocumentItem> = emptyList(),
    val activityRecords: List<ActivityRecord> = emptyList()
)

data class UserSession(
    val role: UserRole,
    val userId: String
)
