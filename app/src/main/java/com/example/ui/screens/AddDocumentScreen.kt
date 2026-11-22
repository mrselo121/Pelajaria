package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.DocumentAiProcessor
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PelajariaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

private enum class UploadChoice {
    ORIGINAL_WORKSHEET,
    ORIGINAL_MATERIAL,
    INTERACTIVE_MATERIAL,
    DIGITAL_QUESTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    teacher: TeacherAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPreset by remember { mutableStateOf(DocumentAiProcessor.SampleDocumentPreset.MATH_WORKSHEET) }
    var uploadedUriString by remember { mutableStateOf("") }
    var format by remember { mutableStateOf(DocumentFormat.IMAGE) }
    var title by remember { mutableStateOf(selectedPreset.title) }
    var description by remember { mutableStateOf(selectedPreset.description) }
    var subject by remember { mutableStateOf(selectedPreset.subject) }
    var level by remember { mutableStateOf(selectedPreset.level) }
    var grade by remember { mutableStateOf(selectedPreset.grade) }
    var dueDate by remember { mutableStateOf("Besok, 19:00") }
    var selectedStudentIds by remember { mutableStateOf(appState.students.map { it.id }.toSet()) }

    var makeInteractiveToggle by remember { mutableStateOf(false) }
    var choice by remember { mutableStateOf(UploadChoice.ORIGINAL_WORKSHEET) }

    var isProcessingAi by remember { mutableStateOf(false) }
    var previewInteractiveSections by remember { mutableStateOf<List<InteractiveSection>>(emptyList()) }
    var previewDigitalQuestions by remember { mutableStateOf<List<DigitalQuestion>>(emptyList()) }
    var ocrWarning by remember { mutableStateOf<String?>(null) }
    var showAiPreviewStep by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Camera capture (zero-permission TakePicturePreview)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedUri = viewModel.saveBitmapToAppStorage(bitmap, "guru_foto")
            uploadedUriString = savedUri
            format = DocumentFormat.IMAGE
            showAiPreviewStep = false
            viewModel.showFeedback("Foto dokumen berhasil diambil dari kamera.")
        } else {
            viewModel.showFeedback("Kamera dibatalkan atau tidak tersedia; gunakan Galeri atau Contoh Dokumen.")
        }
    }

    // Photo Picker (zero-permission)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            uploadedUriString = uri.toString()
            format = DocumentFormat.IMAGE
            showAiPreviewStep = false
            viewModel.showFeedback("Gambar berhasil dipilih dari perangkat.")
        }
    }

    // PDF Picker via SAF
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            uploadedUriString = uri.toString()
            format = DocumentFormat.PDF
            showAiPreviewStep = false
            viewModel.showFeedback("File PDF berhasil dipilih.")
        }
    }

    val subjects = listOf("Matematika", "Bahasa Indonesia", "IPA", "Pendidikan Pancasila", "Bahasa Inggris")
    val grades = if (level == EducationLevel.TK) listOf("TK A", "TK B") else listOf("1 SD", "2 SD", "3 SD", "4 SD", "5 SD", "6 SD")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (showAiPreviewStep) "Preview Hasil Pengolahan" else "Tambah Materi atau Soal",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showAiPreviewStep) showAiPreviewStep = false else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SurfaceTeacherBg
    ) { padding ->
        if (showAiPreviewStep) {
            // --- AI PREVIEW & TEACHER APPROVAL STEP ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(color = PelajariaBlueSoft, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = PelajariaBlue)
                        Text(
                            text = if (choice == UploadChoice.INTERACTIVE_MATERIAL) {
                                "Hasil diolah langsung dari dokumen \"$title\". Tinjau bagian interaktif sebelum menyetujui dan membagikan ke murid."
                            } else {
                                "Soal diekstrak dari dokumen \"$title\". Sunting teks soal atau pilihan jawaban bila perlu sebelum diterbitkan."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = PelajariaBlueDeep
                        )
                    }
                }

                if (!ocrWarning.isNullOrBlank()) {
                    Surface(color = PelajariaAmberSoft, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = PelajariaOrange)
                            Text(ocrWarning!!, style = MaterialTheme.typography.bodySmall, color = Color(0xFF8A4B00))
                        }
                    }
                }

                if (choice == UploadChoice.INTERACTIVE_MATERIAL) {
                    previewInteractiveSections.forEachIndexed { idx, sec ->
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(sec.visualEmoji, style = MaterialTheme.typography.headlineMedium)
                                    Text(sec.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                OutlinedTextField(
                                    value = sec.explanation,
                                    onValueChange = { updated ->
                                        previewInteractiveSections = previewInteractiveSections.mapIndexed { i, s ->
                                            if (i == idx) s.copy(explanation = updated) else s
                                        }
                                    },
                                    label = { Text("Penjelasan Bagian ${idx + 1} (dari dokumen)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Surface(color = PelajariaTealSoft, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "Elemen Sentuh: ${sec.touchPrompt} → ${sec.touchReveal}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PelajariaTeal,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Editable Digital Questions extracted from source document
                    previewDigitalQuestions.forEachIndexed { qIdx, q ->
                        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Soal ${qIdx + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PelajariaPurple)
                                    Surface(color = SurfaceLight, shape = RoundedCornerShape(50)) {
                                        Text(
                                            if (q.isEssay) "Esai • Nilai Manual" else "Pilihan Ganda",
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (q.sourceNote.isNotBlank()) {
                                    Text(q.sourceNote, style = MaterialTheme.typography.labelSmall, color = InkMuted)
                                }
                                OutlinedTextField(
                                    value = q.prompt,
                                    onValueChange = { updatedPrompt ->
                                        previewDigitalQuestions = previewDigitalQuestions.mapIndexed { i, item ->
                                            if (i == qIdx) item.copy(prompt = updatedPrompt) else item
                                        }
                                    },
                                    label = { Text("Teks Pertanyaan") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                if (!q.isEssay) {
                                    q.options.forEachIndexed { oIdx, opt ->
                                        OutlinedTextField(
                                            value = opt,
                                            onValueChange = { updatedOpt ->
                                                previewDigitalQuestions = previewDigitalQuestions.mapIndexed { i, item ->
                                                    if (i == qIdx) {
                                                        val newOpts = item.options.toMutableList()
                                                        newOpts[oIdx] = updatedOpt
                                                        item.copy(options = newOpts)
                                                    } else item
                                                }
                                            },
                                            label = { Text("Pilihan ${'A' + oIdx}") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showAiPreviewStep = false },
                        modifier = Modifier.weight(1f).height(52.dp).testTag("cancel_ai_preview_btn")
                    ) {
                        Text("Batalkan")
                    }
                    Button(
                        onClick = {
                            val newDoc = DocumentItem(
                                id = "doc_${UUID.randomUUID().toString().take(8)}",
                                title = title.trim(),
                                description = description.trim(),
                                subject = subject,
                                level = level,
                                grade = grade,
                                format = format,
                                presentationMode = if (choice == UploadChoice.INTERACTIVE_MATERIAL) {
                                    PresentationMode.INTERACTIVE_MATERIAL
                                } else {
                                    PresentationMode.DIGITAL_QUESTIONS
                                },
                                teacherId = teacher.id,
                                teacherName = teacher.name,
                                targetStudentIds = selectedStudentIds.toList(),
                                dueDate = if (choice == UploadChoice.DIGITAL_QUESTIONS) dueDate else "",
                                sourceUriString = uploadedUriString,
                                samplePresetKey = selectedPreset.key,
                                pdfSimulatedPages = if (format == DocumentFormat.PDF) selectedPreset.pdfPages else emptyList(),
                                interactiveSections = if (choice == UploadChoice.INTERACTIVE_MATERIAL) previewInteractiveSections else emptyList(),
                                digitalQuestions = if (choice == UploadChoice.DIGITAL_QUESTIONS) previewDigitalQuestions else emptyList(),
                                hasOcrWarning = !ocrWarning.isNullOrBlank(),
                                ocrWarningText = ocrWarning ?: "",
                                createdAtLabel = "Baru saja"
                            )
                            viewModel.publishTeacherDocument(newDoc)
                            onBack()
                        },
                        modifier = Modifier.weight(1f).height(52.dp).testTag("approve_ai_publish_btn")
                    ) {
                        Text("Setujui & Bagikan")
                    }
                }
            }
            return@Scaffold
        }

        // --- MAIN UPLOAD & CONFIGURATION STEP ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Upload Foto / PDF
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("1. Unggah Sumber Dokumen (Foto atau PDF)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                runCatching { cameraLauncher.launch(null) }.onFailure {
                                    viewModel.showFeedback("Kamera tidak tersedia di perangkat ini; gunakan Galeri atau Contoh Dokumen.")
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("upload_camera_btn")
                        ) {
                            Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kamera", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f).testTag("upload_gallery_btn")
                        ) {
                            Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gambar", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            },
                            modifier = Modifier.weight(1f).testTag("upload_pdf_btn")
                        ) {
                            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", maxLines = 1)
                        }
                    }

                    Text("Atau pilih contoh file dokumen siap uji:", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(DocumentAiProcessor.SampleDocumentPreset.entries) { preset ->
                            FilterChip(
                                selected = selectedPreset == preset && uploadedUriString.isBlank(),
                                onClick = {
                                    selectedPreset = preset
                                    uploadedUriString = ""
                                    format = preset.format
                                    title = preset.title
                                    description = preset.description
                                    subject = preset.subject
                                    level = preset.level
                                    grade = preset.grade
                                },
                                label = { Text("${preset.title.take(18)} (${if (preset.format == DocumentFormat.PDF) "PDF" else "Foto"})") }
                            )
                        }
                    }

                    // Live Preview File
                    if (format == DocumentFormat.IMAGE) {
                        ZoomableDocumentViewer(
                            uriString = uploadedUriString,
                            samplePresetKey = selectedPreset.key,
                            title = title
                        )
                    } else {
                        PdfDocumentViewer(
                            uriString = uploadedUriString,
                            simulatedPages = selectedPreset.pdfPages,
                            title = title
                        )
                    }
                }
            }

            // 2. Metadata & Target Recipients
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("2. Informasi Dokumen & Penerima", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Materi / Tugas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("doc_title_input")
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi atau Instruksi untuk Murid") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Mata Pelajaran", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { s ->
                            FilterChip(selected = subject == s, onClick = { subject = s }, label = { Text(s) })
                        }
                    }
                    Text("Jenjang & Kelas", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EducationLevel.entries.forEach { lvl ->
                            FilterChip(
                                selected = level == lvl,
                                onClick = {
                                    level = lvl
                                    grade = if (lvl == EducationLevel.TK) "TK B" else "2 SD"
                                },
                                label = { Text(lvl.label) }
                            )
                        }
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(grades) { g ->
                            FilterChip(selected = grade == g, onClick = { grade = g }, label = { Text(g) })
                        }
                    }
                    Text("Murid Penerima (${selectedStudentIds.size} dipilih)", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(appState.students) { st ->
                            val picked = selectedStudentIds.contains(st.id)
                            FilterChip(
                                selected = picked,
                                onClick = {
                                    selectedStudentIds = if (picked && selectedStudentIds.size > 1) {
                                        selectedStudentIds - st.id
                                    } else {
                                        selectedStudentIds + st.id
                                    }
                                },
                                label = { Text("${st.avatarEmoji} ${st.name}") }
                            )
                        }
                    }
                }
            }

            // 3. Presentation Mode (Pilihan A vs Pilihan B vs Soal Digital)
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("3. Cara Penyajian ke Murid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Pilihan B Toggle: Jadikan Materi Interaktif
                    Surface(
                        color = if (makeInteractiveToggle) PelajariaTealSoft else SurfaceLight,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (makeInteractiveToggle) PelajariaTeal else OutlineSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pilihan B: Jadikan Materi Interaktif", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PelajariaTeal)
                                Text(
                                    "AI membaca dokumen sumber & menyusunnya menjadi bagian bertahap dengan ilustrasi & elemen sentuh. Wajib ditinjau guru.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkSecondary
                                )
                            }
                            Switch(
                                checked = makeInteractiveToggle,
                                onCheckedChange = { checked ->
                                    makeInteractiveToggle = checked
                                    choice = if (checked) UploadChoice.INTERACTIVE_MATERIAL else UploadChoice.ORIGINAL_WORKSHEET
                                },
                                modifier = Modifier.testTag("toggle_interactive_material")
                            )
                        }
                    }

                    if (!makeInteractiveToggle) {
                        Text("Pilihan A: Berikan Dokumen Asli (atau Ubah Jadi Soal Digital)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                        ModeRadioOption(
                            selected = choice == UploadChoice.ORIGINAL_WORKSHEET,
                            title = "Dokumen Asli: Soal atau Lembar Kerja",
                            subtitle = "Murid melihat foto/PDF asli, lalu mengirim jawaban teks atau foto. Guru menilai secara manual.",
                            accent = PelajariaOrange,
                            testTag = "mode_original_worksheet"
                        ) { choice = UploadChoice.ORIGINAL_WORKSHEET }

                        ModeRadioOption(
                            selected = choice == UploadChoice.ORIGINAL_MATERIAL,
                            title = "Dokumen Asli: Materi Bacaan",
                            subtitle = "Murid membaca foto/PDF apa adanya tanpa kewajiban mengerjakan kuis.",
                            accent = PelajariaBlueBright,
                            testTag = "mode_original_material"
                        ) { choice = UploadChoice.ORIGINAL_MATERIAL }

                        ModeRadioOption(
                            selected = choice == UploadChoice.DIGITAL_QUESTIONS,
                            title = "Opsi Soal: Ubah Menjadi Soal Digital",
                            subtitle = "Ekstrak soal dari foto/PDF dokumen sumber agar murid menjawab langsung di aplikasi (perlu pemeriksaan guru).",
                            accent = PelajariaPurple,
                            testTag = "mode_digital_questions"
                        ) { choice = UploadChoice.DIGITAL_QUESTIONS }
                    }

                    if (choice == UploadChoice.ORIGINAL_WORKSHEET || choice == UploadChoice.DIGITAL_QUESTIONS) {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("Batas Waktu Pengumpulan") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (errorText != null) {
                        Text(errorText!!, color = PelajariaCoral, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorText = "Judul dokumen tidak boleh kosong."
                                return@Button
                            }
                            errorText = null
                            if (choice == UploadChoice.INTERACTIVE_MATERIAL || choice == UploadChoice.DIGITAL_QUESTIONS) {
                                scope.launch {
                                    isProcessingAi = true
                                    delay(750)
                                    if (choice == UploadChoice.INTERACTIVE_MATERIAL) {
                                        previewInteractiveSections = DocumentAiProcessor.extractInteractiveFromDocument(
                                            docTitle = title.trim(),
                                            docDescription = description.trim(),
                                            subject = subject,
                                            level = level,
                                            presetKey = selectedPreset.key,
                                            sourcePages = selectedPreset.pdfPages
                                        )
                                        ocrWarning = null
                                    } else {
                                        val res = DocumentAiProcessor.convertDocumentToDigitalQuestions(
                                            docTitle = title.trim(),
                                            subject = subject,
                                            presetKey = selectedPreset.key,
                                            hasUploadedCustomFile = uploadedUriString.isNotBlank()
                                        )
                                        previewDigitalQuestions = res.questions
                                        ocrWarning = if (res.hasWarning) res.warningMessage else null
                                    }
                                    isProcessingAi = false
                                    showAiPreviewStep = true
                                }
                            } else {
                                val mode = if (choice == UploadChoice.ORIGINAL_MATERIAL) {
                                    PresentationMode.ORIGINAL_MATERIAL
                                } else {
                                    PresentationMode.ORIGINAL_WORKSHEET
                                }
                                val doc = DocumentItem(
                                    id = "doc_${UUID.randomUUID().toString().take(8)}",
                                    title = title.trim(),
                                    description = description.trim(),
                                    subject = subject,
                                    level = level,
                                    grade = grade,
                                    format = format,
                                    presentationMode = mode,
                                    teacherId = teacher.id,
                                    teacherName = teacher.name,
                                    targetStudentIds = selectedStudentIds.toList(),
                                    dueDate = if (mode == PresentationMode.ORIGINAL_WORKSHEET) dueDate else "",
                                    sourceUriString = uploadedUriString,
                                    samplePresetKey = selectedPreset.key,
                                    pdfSimulatedPages = if (format == DocumentFormat.PDF) selectedPreset.pdfPages else emptyList(),
                                    createdAtLabel = "Baru saja"
                                )
                                viewModel.publishTeacherDocument(doc)
                                onBack()
                            }
                        },
                        enabled = !isProcessingAi,
                        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("publish_or_preview_btn")
                    ) {
                        Text(
                            text = when {
                                isProcessingAi -> "Memproses Dokumen Sumber..."
                                choice == UploadChoice.INTERACTIVE_MATERIAL || choice == UploadChoice.DIGITAL_QUESTIONS ->
                                    "Proses Dokumen & Tinjau Hasil"
                                else -> "Bagikan Dokumen ke Murid"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (isProcessingAi) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun ModeRadioOption(
    selected: Boolean,
    title: String,
    subtitle: String,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (selected) accent.copy(alpha = 0.09f) else SurfaceLight,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (selected) accent else OutlineSubtle)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            }
        }
    }
}
