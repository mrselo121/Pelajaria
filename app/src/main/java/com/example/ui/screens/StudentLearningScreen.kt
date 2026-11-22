package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NavigateBefore
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PelajariaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLearningScreen(
    documentId: String,
    student: StudentProfile,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val doc = appState.documents.find { it.id == documentId }

    var interactiveStep by remember(documentId) { mutableIntStateOf(0) }
    var revealedTouchCards by remember(documentId) { mutableStateOf<Set<String>>(emptySet()) }

    val existingSub = doc?.submissions?.get(student.id)
    var textAnswer by remember(documentId) { mutableStateOf(existingSub?.textAnswer ?: "") }
    var photoUriString by remember(documentId) { mutableStateOf(existingSub?.photoUriString ?: "") }
    var samplePhotoKey by remember(documentId) { mutableStateOf(existingSub?.sampleAnswerPhotoKey ?: "") }
    var digitalAnswers by remember(documentId) { mutableStateOf(existingSub?.digitalAnswers ?: emptyMap()) }
    var submitError by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            photoUriString = viewModel.saveBitmapToAppStorage(bitmap, "murid_jawaban")
            samplePhotoKey = ""
            viewModel.showFeedback("Foto pekerjaan berhasil diambil!")
        } else {
            viewModel.showFeedback("Kamera tidak tersedia; gunakan Galeri atau Contoh Foto Jawaban.")
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            photoUriString = uri.toString()
            samplePhotoKey = ""
            viewModel.showFeedback("Foto pekerjaan berhasil dipilih!")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(doc?.title ?: "Halaman Belajar", fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("student_back_btn")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SurfaceChildBg
    ) { padding ->
        if (doc == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateCard(icon = Icons.Rounded.ErrorOutline, title = "Dokumen Tidak Ditemukan", subtitle = "Kembali ke daftar belajar.")
            }
            return@Scaffold
        }

        val isRead = doc.readByStudentIds.contains(student.id)
        val isSubmittedOrGraded = existingSub != null && existingSub.status != SubmissionStatus.NOT_STARTED

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PresentationModeBadge(doc.presentationMode)
                        if (doc.isAssignment) {
                            SubmissionStatusBadge(existingSub?.status ?: SubmissionStatus.NOT_STARTED)
                        }
                    }
                    Text(doc.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Text("${doc.subject} • ${doc.teacherName}", style = MaterialTheme.typography.labelLarge, color = PelajariaBlue)
                    Text(doc.description, style = MaterialTheme.typography.bodyMedium, color = InkPrimary)
                }
            }

            // Show Teacher Score & Note if Graded
            if (existingSub?.status == SubmissionStatus.GRADED) {
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = PelajariaGreenSoft)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Hasil Penilaian Guru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PelajariaGreen)
                            Surface(color = PelajariaGreen, shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    "Nilai ${existingSub.score ?: 0}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        if (existingSub.teacherNote.isNotBlank()) {
                            Text("Catatan ${doc.teacherName}: \"${existingSub.teacherNote}\"", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // --- CASE 1: INTERACTIVE MATERIAL (STEP-BY-STEP WITH ANIMATIONS & TOUCH ELEMENTS) ---
            if (doc.presentationMode == PresentationMode.INTERACTIVE_MATERIAL && doc.interactiveSections.isNotEmpty()) {
                val sections = doc.interactiveSections
                val safeIdx = interactiveStep.coerceIn(0, sections.lastIndex)
                val sec = sections[safeIdx]
                val isTouchRevealed = revealedTouchCards.contains(sec.id)
                val emojiScale by animateFloatAsState(
                    targetValue = if (isTouchRevealed) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "emojiScale"
                )

                // Step Indicator
                LinearProgressIndicator(
                    progress = { (safeIdx + 1).toFloat() / sections.size.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
                    color = PelajariaTeal,
                    trackColor = PelajariaBlueSoft
                )
                Text(
                    "Bagian ${safeIdx + 1} dari ${sections.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = PelajariaTeal,
                    fontWeight = FontWeight.Bold
                )

                AnimatedContent(
                    targetState = sec,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "interactiveSectionTransition"
                ) { currentSec ->
                    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(PelajariaBlueSoft.copy(alpha = 0.55f))
                                    .padding(vertical = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentSec.visualEmoji,
                                    fontSize = 54.sp,
                                    modifier = Modifier.scale(emojiScale)
                                )
                            }
                            Text(currentSec.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Text(currentSec.explanation, style = MaterialTheme.typography.bodyLarge, color = InkPrimary)

                            Surface(color = PelajariaAmberSoft, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Contoh Sederhana:", style = MaterialTheme.typography.labelLarge, color = Color(0xFF9A5B00), fontWeight = FontWeight.Bold)
                                    Text(currentSec.simpleExample, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            // Touchable Interactive Visual Element
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        revealedTouchCards = if (isTouchRevealed) {
                                            revealedTouchCards - currentSec.id
                                        } else {
                                            revealedTouchCards + currentSec.id
                                        }
                                    }
                                    .testTag("interactive_touch_card_${currentSec.id}"),
                                color = if (isTouchRevealed) PelajariaTealSoft else PelajariaBlueSoft,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, PelajariaTeal)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Rounded.TouchApp, contentDescription = null, tint = PelajariaTeal)
                                        Text(currentSec.touchPrompt, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = PelajariaTeal)
                                    }
                                    AnimatedVisibility(visible = isTouchRevealed) {
                                        Text(currentSec.touchReveal, style = MaterialTheme.typography.bodyMedium, color = InkPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Previous / Next Step Controls
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { if (safeIdx > 0) interactiveStep = safeIdx - 1 },
                        enabled = safeIdx > 0,
                        modifier = Modifier.weight(1f).height(52.dp).testTag("interactive_prev_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.NavigateBefore, contentDescription = null)
                        Text("Sebelumnya")
                    }
                    if (safeIdx < sections.lastIndex) {
                        Button(
                            onClick = { interactiveStep = safeIdx + 1 },
                            modifier = Modifier.weight(1f).height(52.dp).testTag("interactive_next_btn"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Berikutnya")
                            Icon(Icons.AutoMirrored.Rounded.NavigateNext, contentDescription = null)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.markMaterialAsRead(doc.id, student.id) },
                            enabled = !isRead,
                            modifier = Modifier.weight(1f).height(52.dp).testTag("mark_read_btn"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PelajariaGreen)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isRead) "Sudah Dibaca" else "Selesai Baca")
                        }
                    }
                }
            } else {
                // --- CASE 2: ORIGINAL DOCUMENT (ZOOMABLE PHOTO OR PDF READER) ---
                if (doc.format == DocumentFormat.IMAGE) {
                    ZoomableDocumentViewer(
                        uriString = doc.sourceUriString,
                        samplePresetKey = doc.samplePresetKey,
                        title = doc.title
                    )
                } else {
                    PdfDocumentViewer(
                        uriString = doc.sourceUriString,
                        simulatedPages = doc.pdfSimulatedPages,
                        title = doc.title
                    )
                }

                // If Original Reading Material -> Mark as Read button (no quiz!)
                if (doc.presentationMode == PresentationMode.ORIGINAL_MATERIAL) {
                    Button(
                        onClick = { viewModel.markMaterialAsRead(doc.id, student.id) },
                        enabled = !isRead,
                        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("mark_read_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRead) PelajariaGreen else PelajariaBlueBright)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRead) "Materi Sudah Dibaca ✓" else "Tandai Sudah Dibaca", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- CASE 3: ASSIGNMENT SUBMISSION AREA (ORIGINAL WORKSHEET OR DIGITAL QUESTIONS) ---
            if (doc.isAssignment) {
                if (doc.presentationMode == PresentationMode.DIGITAL_QUESTIONS && doc.digitalQuestions.isNotEmpty()) {
                    Text("Soal Digital dari Dokumen", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    doc.digitalQuestions.forEachIndexed { idx, q ->
                        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Pertanyaan ${idx + 1}", style = MaterialTheme.typography.labelLarge, color = PelajariaPurple, fontWeight = FontWeight.Bold)
                                Text(q.prompt, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                if (q.isEssay || q.options.isEmpty()) {
                                    OutlinedTextField(
                                        value = digitalAnswers[q.id] ?: "",
                                        onValueChange = { valTxt -> digitalAnswers = digitalAnswers + (q.id to valTxt) },
                                        enabled = !isSubmittedOrGraded,
                                        label = { Text("Tulis jawabanmu di sini") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    q.options.forEach { opt ->
                                        val picked = digitalAnswers[q.id] == opt
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable(enabled = !isSubmittedOrGraded) {
                                                    digitalAnswers = digitalAnswers + (q.id to opt)
                                                },
                                            color = if (picked) PelajariaBlueSoft else SurfaceLight,
                                            border = androidx.compose.foundation.BorderStroke(1.5.dp, if (picked) PelajariaBlueBright else OutlineSubtle)
                                        ) {
                                            Text(opt, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Text / Photo Submission Card
                Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Kirim Jawaban ke Guru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = textAnswer,
                            onValueChange = { textAnswer = it },
                            enabled = !isSubmittedOrGraded,
                            label = { Text("Ketik jawaban atau catatan untuk guru") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("student_text_answer_input")
                        )

                        if (!isSubmittedOrGraded) {
                            Text("Atau unggah foto hasil tulisan tangan:", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        runCatching { cameraLauncher.launch(null) }.onFailure {
                                            viewModel.showFeedback("Kamera tidak tersedia; gunakan Galeri atau Contoh Foto.")
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kamera", maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Galeri", maxLines = 1)
                                }
                            }
                            TextButton(
                                onClick = {
                                    samplePhotoKey = "handwritten_math_sample"
                                    photoUriString = ""
                                },
                                modifier = Modifier.testTag("use_sample_answer_photo_btn")
                            ) {
                                Text("Gunakan Contoh Foto Pekerjaan Tulisan Tangan")
                            }
                        }

                        if (photoUriString.isNotBlank() || samplePhotoKey.isNotBlank()) {
                            ZoomableDocumentViewer(
                                uriString = photoUriString,
                                samplePresetKey = samplePhotoKey.ifBlank { "handwritten_math_sample" },
                                title = "Foto Pekerjaan ${student.name}"
                            )
                        }

                        if (submitError != null) {
                            Text(submitError!!, color = PelajariaCoral, style = MaterialTheme.typography.bodySmall)
                        }

                        if (!isSubmittedOrGraded) {
                            Button(
                                onClick = {
                                    val hasDigital = digitalAnswers.values.any { it.isNotBlank() }
                                    val hasTextOrPhoto = textAnswer.isNotBlank() || photoUriString.isNotBlank() || samplePhotoKey.isNotBlank()
                                    if (!hasDigital && !hasTextOrPhoto) {
                                        submitError = "Tulis jawaban atau unggah foto pekerjaan terlebih dahulu."
                                    } else {
                                        submitError = null
                                        viewModel.submitStudentWork(
                                            documentId = doc.id,
                                            studentId = student.id,
                                            textAnswer = textAnswer,
                                            photoUriString = photoUriString,
                                            sampleAnswerPhotoKey = samplePhotoKey,
                                            digitalAnswers = digitalAnswers
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(54.dp).testTag("student_submit_work_btn"),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Rounded.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Kirim Jawaban", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(color = PelajariaBlueSoft, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (existingSub?.status == SubmissionStatus.WAITING_GRADE) {
                                        "Jawaban sudah dikirim (${existingSub.submittedAtLabel}) dan sedang menunggu penilaian manual guru."
                                    } else {
                                        "Tugas ini sudah selesai dan dinilai oleh guru."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PelajariaBlueDeep,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Explicit Back Button at bottom too
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Kembali")
            }
        }
    }
}
