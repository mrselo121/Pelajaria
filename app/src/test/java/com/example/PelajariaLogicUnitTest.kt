package com.example

import com.example.data.DocumentAiProcessor
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class PelajariaLogicUnitTest {

    @Test
    fun `presentation mode assignment classification is accurate`() {
        val baseDoc = DocumentItem(
            id = "d1",
            title = "Contoh",
            description = "Desc",
            subject = "Matematika",
            level = EducationLevel.SD,
            grade = "2 SD",
            format = DocumentFormat.IMAGE,
            presentationMode = PresentationMode.ORIGINAL_WORKSHEET,
            teacherId = "t1",
            teacherName = "Ibu Siti",
            targetStudentIds = listOf("student_alya")
        )
        assertTrue(baseDoc.isAssignment)
        assertTrue(baseDoc.copy(presentationMode = PresentationMode.DIGITAL_QUESTIONS).isAssignment)
        assertFalse(baseDoc.copy(presentationMode = PresentationMode.ORIGINAL_MATERIAL).isAssignment)
        assertFalse(baseDoc.copy(presentationMode = PresentationMode.INTERACTIVE_MATERIAL).isAssignment)
    }

    @Test
    fun `document ai processor extracts interactive sections strictly from document`() {
        val sections = DocumentAiProcessor.extractInteractiveFromDocument(
            docTitle = "Materi Mengenal Hewan",
            docDescription = "Habitat hewan",
            subject = "IPA",
            level = EducationLevel.SD,
            presetKey = "animal_material",
            sourcePages = DocumentAiProcessor.SampleDocumentPreset.ANIMAL_MATERIAL.pdfPages
        )
        assertEquals(3, sections.size)
        assertTrue(sections.first().explanation.contains("Materi Mengenal Hewan"))
        assertTrue(sections.all { it.touchPrompt.isNotBlank() && it.touchReveal.isNotBlank() })
    }

    @Test
    fun `document ai processor converts document to digital questions with ocr warning for blurry handwriting`() {
        val result = DocumentAiProcessor.convertDocumentToDigitalQuestions(
            docTitle = "Latihan Bahasa Indonesia",
            subject = "Bahasa Indonesia",
            presetKey = "indo_handwritten",
            hasUploadedCustomFile = false
        )
        assertTrue(result.hasWarning)
        assertTrue(result.warningMessage.contains("kurang jelas"))
        assertEquals(2, result.questions.size)
        assertFalse(result.questions[0].isEssay)
        assertTrue(result.questions[1].isEssay)
    }
}
