package com.example.data

import com.example.model.*
import java.util.UUID

/**
 * Simulates AI processing STRICTLY from a document uploaded/selected by the Teacher.
 * Never generates free-form topics or questions from scratch.
 */
object DocumentAiProcessor {

    enum class SampleDocumentPreset(
        val key: String,
        val title: String,
        val subject: String,
        val format: DocumentFormat,
        val level: EducationLevel,
        val grade: String,
        val description: String,
        val isHandwrittenOrBlurry: Boolean,
        val pdfPages: List<String>
    ) {
        MATH_WORKSHEET(
            key = "math_worksheet",
            title = "Lembar Kerja Penjumlahan",
            subject = "Matematika",
            format = DocumentFormat.IMAGE,
            level = EducationLevel.SD,
            grade = "2 SD",
            description = "Foto lembar kerja soal cerita penjumlahan bilangan dua angka.",
            isHandwrittenOrBlurry = false,
            pdfPages = emptyList()
        ),
        ANIMAL_MATERIAL(
            key = "animal_material",
            title = "Materi Mengenal Hewan",
            subject = "IPA",
            format = DocumentFormat.PDF,
            level = EducationLevel.SD,
            grade = "2 SD",
            description = "Dokumen PDF ringkasan jenis hewan dan tempat hidupnya di alam.",
            isHandwrittenOrBlurry = false,
            pdfPages = listOf(
                "Halaman 1 — Mengenal Hewan di Sekitar Kita\n\nHewan hidup di tempat yang berbeda sesuai kebutuhan tubuhnya. Tempat tinggal alami hewan disebut habitat.\n\n1. Hewan Darat: Kucing, kelinci, dan kambing berjalan di atas tanah.\n2. Hewan Air: Ikan mas dan lumba-lumba bernapas di dalam air.",
                "Halaman 2 — Hewan Dua Alam & Cara Merawat Hewan\n\nAda pula hewan yang hidup di darat dan air seperti katak.\n\nMenyayangi hewan dapat dilakukan dengan memberi makan teratur dan menjaga kebersihan kandang."
            )
        ),
        INDONESIAN_HANDWRITTEN(
            key = "indo_handwritten",
            title = "Latihan Bahasa Indonesia",
            subject = "Bahasa Indonesia",
            format = DocumentFormat.IMAGE,
            level = EducationLevel.SD,
            grade = "2 SD",
            description = "Foto catatan tulisan tangan soal huruf vokal, suku kata, dan kalimat santun.",
            isHandwrittenOrBlurry = true,
            pdfPages = emptyList()
        ),
        ENVIRONMENT_PDF(
            key = "environment_pdf",
            title = "Mengenal Lingkungan Sekitar",
            subject = "Pendidikan Pancasila",
            format = DocumentFormat.PDF,
            level = EducationLevel.SD,
            grade = "2 SD",
            description = "Dokumen PDF bacaan tentang menjaga kebersihan halaman rumah dan sekolah.",
            isHandwrittenOrBlurry = false,
            pdfPages = listOf(
                "Halaman 1 — Lingkungan Bersih dan Sehat\n\nLingkungan sekitar adalah tempat kita tinggal, bermain, dan belajar bersama keluarga serta teman.\n\nMembuang sampah pada tempatnya membuat rumah dan kelas terasa nyaman.",
                "Halaman 2 — Gotong Royong Membersihkan Kelas\n\nSetiap hari murid-murid bekerja sama menyapu kelas, merapikan meja, dan menyiram tanaman di taman sekolah."
            )
        ),
        TK_FRUIT_COUNT(
            key = "tk_fruit_count",
            title = "Kartu Hitung Buah Segar",
            subject = "Matematika",
            format = DocumentFormat.IMAGE,
            level = EducationLevel.TK,
            grade = "TK B",
            description = "Foto kartu bergambar buah apel dan jeruk untuk latihan berhitung 1-5.",
            isHandwrittenOrBlurry = false,
            pdfPages = emptyList()
        )
    }

    data class DigitalConversionResult(
        val questions: List<DigitalQuestion>,
        val hasWarning: Boolean,
        val warningMessage: String
    )

    fun extractInteractiveFromDocument(
        docTitle: String,
        docDescription: String,
        subject: String,
        level: EducationLevel,
        presetKey: String,
        sourcePages: List<String>
    ): List<InteractiveSection> {
        val isTk = level == EducationLevel.TK
        return when (presetKey) {
            "animal_material" -> listOf(
                InteractiveSection(
                    id = "sec_1",
                    title = "Bagian 1: Apa Itu Habitat Hewan?",
                    explanation = if (isTk) {
                        "Dari dokumen \"$docTitle\": Setiap hewan punya rumah alami yang disebut habitat."
                    } else {
                        "Bersumber dari halaman 1 dokumen \"$docTitle\": Tempat tinggal alami hewan untuk tumbuh dan berlindung disebut habitat."
                    },
                    simpleExample = "Kucing tinggal di darat, sedangkan ikan mas tinggal di dalam air.",
                    touchPrompt = "Ketuk kartu di bawah untuk melihat ciri hewan air!",
                    touchReveal = "Ikan memiliki sirip untuk berenang dan insang untuk bernapas di air.",
                    visualEmoji = "🐟"
                ),
                InteractiveSection(
                    id = "sec_2",
                    title = "Bagian 2: Hewan Darat & Dua Alam",
                    explanation = "Dari dokumen \"$docTitle\": Kucing, kelinci, dan kambing hidup di darat. Katak dapat hidup di darat dan di air.",
                    simpleExample = "Di halaman rumah kita sering bertemu kucing, sedangkan di kolam ada katak dan ikan.",
                    touchPrompt = "Ketuk untuk menebak contoh hewan dua alam dari dokumen!",
                    touchReveal = "Katak! Katak bisa melompat di darat dan berenang di air.",
                    visualEmoji = "🐸"
                ),
                InteractiveSection(
                    id = "sec_3",
                    title = "Bagian 3: Cara Merawat Hewan",
                    explanation = "Sesuai halaman 2 dokumen \"$docTitle\": Kita menyayangi hewan dengan memberi makan teratur dan menjaga kebersihan tempat tinggalnya.",
                    simpleExample = "Menyiapkan air minum bersih untuk hewan peliharaan setiap pagi.",
                    touchPrompt = "Ketuk bintang untuk melihat pengingat baik hari ini!",
                    touchReveal = "Beri makan teratur dan jaga kandang tetap bersih ya!",
                    visualEmoji = "🐾"
                )
            )
            "environment_pdf" -> listOf(
                InteractiveSection(
                    id = "sec_env_1",
                    title = "Bagian 1: Lingkungan Nyaman",
                    explanation = "Dari halaman 1 dokumen \"$docTitle\": Lingkungan sekitar adalah tempat kita tinggal, bermain, dan belajar.",
                    simpleExample = "Membuang bungkus makanan ke tempat sampah agar halaman tetap bersih.",
                    touchPrompt = "Ketuk untuk melihat kebiasaan baik dari bacaan!",
                    touchReveal = "Buang sampah pada tempatnya membuat rumah dan kelas terasa nyaman.",
                    visualEmoji = "🌿"
                ),
                InteractiveSection(
                    id = "sec_env_2",
                    title = "Bagian 2: Gotong Royong",
                    explanation = "Dari halaman 2 dokumen \"$docTitle\": Murid bekerja sama menyapu kelas, merapikan meja, dan menyiram tanaman.",
                    simpleExample = "Piket kelas bersama teman membuat pekerjaan cepat selesai.",
                    touchPrompt = "Ketuk untuk melihat contoh tugas piket di dokumen!",
                    touchReveal = "Menyapu kelas, merapikan meja, dan menyiram tanaman sekolah.",
                    visualEmoji = "🧹"
                )
            )
            else -> {
                val snippet = sourcePages.firstOrNull()?.take(140) ?: docDescription
                listOf(
                    InteractiveSection(
                        id = "sec_doc_1",
                        title = "Bagian 1: Pokok Bahasan Dokumen",
                        explanation = "Diolah langsung dari dokumen \"$docTitle\" ($subject): $snippet",
                        simpleExample = "Perhatikan instruksi dan contoh yang tertulis pada lembar dokumen guru.",
                        touchPrompt = "Ketuk kartu untuk melihat ringkasan dokumen!",
                        touchReveal = "Fokus utama dokumen ini adalah \"$docTitle\" pada mata pelajaran $subject.",
                        visualEmoji = "📖"
                    ),
                    InteractiveSection(
                        id = "sec_doc_2",
                        title = "Bagian 2: Langkah Memahami Isi",
                        explanation = if (isTk) {
                            "Lihat gambar pada dokumen pelan-pelan dan sebutkan bersama guru atau orang tua."
                        } else {
                            "Baca setiap baris pada dokumen \"$docTitle\" secara berurutan agar tidak ada bagian penting yang terlewat."
                        },
                        simpleExample = "Cocokkan penjelasan di dokumen dengan contoh benda di sekitarmu.",
                        touchPrompt = "Ketuk untuk melihat tips belajar dari dokumen ini!",
                        touchReveal = "Jika ada bagian yang belum jelas, lihat kembali foto atau PDF asli dari guru.",
                        visualEmoji = "✨"
                    )
                )
            }
        }
    }

    fun convertDocumentToDigitalQuestions(
        docTitle: String,
        subject: String,
        presetKey: String,
        hasUploadedCustomFile: Boolean
    ): DigitalConversionResult {
        return when (presetKey) {
            "math_worksheet" -> DigitalConversionResult(
                questions = listOf(
                    DigitalQuestion(
                        id = "dq_${UUID.randomUUID().toString().take(6)}",
                        prompt = "1. Ibu membeli 14 apel merah dan 5 apel hijau. Berapa jumlah seluruh apel Ibu?",
                        isEssay = false,
                        options = listOf("18 apel", "19 apel", "20 apel", "21 apel"),
                        sourceNote = "Diekstrak dari soal no. 1 pada $docTitle"
                    ),
                    DigitalQuestion(
                        id = "dq_${UUID.randomUUID().toString().take(6)}",
                        prompt = "2. Hitung dan tuliskan cara penjumlahan: 12 + 15 = ...",
                        isEssay = true,
                        options = emptyList(),
                        sourceNote = "Soal isian/tulisan dari $docTitle (dinilai manual oleh guru)"
                    )
                ),
                hasWarning = hasUploadedCustomFile,
                warningMessage = if (hasUploadedCustomFile) {
                    "Periksa kembali teks soal hasil pembacaan dokumen unggahan Anda sebelum dibagikan."
                } else ""
            )
            "indo_handwritten" -> DigitalConversionResult(
                questions = listOf(
                    DigitalQuestion(
                        id = "dq_${UUID.randomUUID().toString().take(6)}",
                        prompt = "1. Huruf vokal yang terdapat pada kata 'BUKU' adalah...",
                        isEssay = false,
                        options = listOf("B dan K", "U saja", "B dan U", "K dan U"),
                        sourceNote = "Diekstrak dari tulisan tangan no. 1"
                    ),
                    DigitalQuestion(
                        id = "dq_${UUID.randomUUID().toString().take(6)}",
                        prompt = "2. Tulislah 1 kalimat sapaan santun saat bertemu guru di pagi hari!",
                        isEssay = true,
                        options = emptyList(),
                        sourceNote = "Soal esai tulisan tangan no. 2 (dinilai manual oleh guru)"
                    )
                ),
                hasWarning = true,
                warningMessage = "Sebagian tulisan tangan pada foto kurang jelas. Mohon periksa dan sunting teks soal sebelum diterbitkan."
            )
            "tk_fruit_count" -> DigitalConversionResult(
                questions = listOf(
                    DigitalQuestion(
                        id = "dq_${UUID.randomUUID().toString().take(6)}",
                        prompt = "1. Ada 2 apel dan 1 jeruk di gambar. Ada berapa semua buahnya?",
                        isEssay = false,
                        options = listOf("2 buah", "3 buah", "4 buah"),
                        sourceNote = "Diekstrak dari kartu bergambar"
                    )
                ),
                hasWarning = false,
                warningMessage = ""
            )
            else -> DigitalConversionResult(
                questions = listOf(
                    DigitalQuestion(
                        id = "dq_${UUID.randomUUID().toString().take(6)}",
                        prompt = "1. Jawablah pertanyaan berdasarkan dokumen \"$docTitle\" ($subject) di atas.",
                        isEssay = true,
                        options = emptyList(),
                        sourceNote = "Diekstrak dari dokumen unggahan (penilaian manual oleh guru)"
                    )
                ),
                hasWarning = true,
                warningMessage = "Format dokumen terdeteksi sebagai bacaan/tulisan bebas. Periksa soal sebelum diterbitkan ke murid."
            )
        }
    }
}
