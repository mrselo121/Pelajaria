package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.DocumentAiProcessor
import com.example.model.*
import com.example.viewmodel.PelajariaViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PelajariaFlowTest {

    @Test
    fun `demo data includes Ibu Siti, Alya, Budi Santoso, and required sample documents`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = PelajariaViewModel(app)
        val state = vm.uiState.value.appState

        val siti = state.teachers.find { it.name == "Ibu Siti" }
        assertNotNull(siti)
        assertEquals("Matematika", siti?.subject)

        val alya = state.students.find { it.name == "Alya" }
        assertNotNull(alya)
        assertEquals("2 SD", alya?.grade)

        val budi = state.parents.find { it.name == "Budi Santoso" }
        assertNotNull(budi)
        assertEquals(budi?.id, alya?.parentId)

        val titles = state.documents.map { it.title }
        assertTrue(titles.contains("Lembar Kerja Penjumlahan"))
        assertTrue(titles.contains("Materi Mengenal Hewan"))
        assertTrue(titles.contains("Latihan Bahasa Indonesia"))
        assertTrue(titles.contains("Mengenal Lingkungan Sekitar"))
    }

    @Test
    fun `end-to-end teacher upload, student submit, manual grading, and parent visibility`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = PelajariaViewModel(app)

        // 1. Guru (Ibu Siti) mengunggah dokumen soal & menerbitkan
        vm.loginAsDemo(UserRole.TEACHER, "teacher_siti")
        val newDoc = DocumentItem(
            id = "doc_test_perkalian",
            title = "Lembar Soal Perkalian",
            description = "Kerjakan soal perkalian dari foto guru.",
            subject = "Matematika",
            level = EducationLevel.SD,
            grade = "2 SD",
            format = DocumentFormat.IMAGE,
            presentationMode = PresentationMode.ORIGINAL_WORKSHEET,
            teacherId = "teacher_siti",
            teacherName = "Ibu Siti",
            targetStudentIds = listOf("student_alya"),
            dueDate = "Besok"
        )
        vm.publishTeacherDocument(newDoc)

        // 2. Murid (Alya) menerima & mengirim jawaban
        val alyaTasks = vm.studentAssignments("student_alya")
        assertTrue(alyaTasks.any { it.id == "doc_test_perkalian" })

        vm.submitStudentWork(
            documentId = "doc_test_perkalian",
            studentId = "student_alya",
            textAnswer = "2 x 4 = 8",
            photoUriString = "",
            sampleAnswerPhotoKey = "handwritten_math_sample",
            digitalAnswers = emptyMap()
        )
        val afterSubmit = vm.uiState.value.appState.documents.first { it.id == "doc_test_perkalian" }
        assertEquals(SubmissionStatus.WAITING_GRADE, afterSubmit.submissions["student_alya"]?.status)

        // 3. Guru memeriksa & memberi nilai manual
        vm.saveManualGrade(
            documentId = "doc_test_perkalian",
            studentId = "student_alya",
            score = 95,
            teacherNote = "Hebat sekali Alya!"
        )

        // 4. Nilai & catatan tampil untuk Murid & Orang Tua
        val gradedDoc = vm.uiState.value.appState.documents.first { it.id == "doc_test_perkalian" }
        val submission = gradedDoc.submissions["student_alya"]
        assertEquals(SubmissionStatus.GRADED, submission?.status)
        assertEquals(95, submission?.score)
        assertEquals("Hebat sekali Alya!", submission?.teacherNote)
    }

    @Test
    fun `document ai processor extracts interactive sections and flags blurry handwriting`() {
        val sections = DocumentAiProcessor.extractInteractiveFromDocument(
            docTitle = "Materi Mengenal Hewan",
            docDescription = "Habitat hewan",
            subject = "IPA",
            level = EducationLevel.SD,
            presetKey = "animal_material",
            sourcePages = emptyList()
        )
        assertTrue(sections.size >= 3)

        val ocrResult = DocumentAiProcessor.convertDocumentToDigitalQuestions(
            docTitle = "Latihan Bahasa Indonesia",
            subject = "Bahasa Indonesia",
            presetKey = "indo_handwritten",
            hasUploadedCustomFile = false
        )
        assertTrue(ocrResult.hasWarning)
        assertTrue(ocrResult.questions.isNotEmpty())
    }
}
