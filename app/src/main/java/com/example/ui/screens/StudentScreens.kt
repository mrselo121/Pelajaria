package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PelajariaViewModel
import com.example.viewmodel.SubRoute

@Composable
fun StudentHomeTab(
    student: StudentProfile,
    viewModel: PelajariaViewModel
) {
    val materials = viewModel.studentMaterials(student.id)
    val assignments = viewModel.studentAssignments(student.id)
    val unreadMaterials = materials.filter { !it.readByStudentIds.contains(student.id) }
    val pendingTasks = assignments.filter {
        (it.submissions[student.id]?.status ?: SubmissionStatus.NOT_STARTED) == SubmissionStatus.NOT_STARTED
    }
    val gradedTasks = assignments.filter { it.submissions[student.id]?.status == SubmissionStatus.GRADED }

    val totalItems = (materials.size + assignments.size).coerceAtLeast(1)
    val doneItems = materials.count { it.readByStudentIds.contains(student.id) } +
        assignments.count { (it.submissions[student.id]?.status ?: SubmissionStatus.NOT_STARTED) != SubmissionStatus.NOT_STARTED }
    val progress = doneItems.toFloat() / totalItems.toFloat()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceChildBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cheerful Student Greeting Card
        item {
            Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(PelajariaBlueBright, Color(0xFF42A5F5))))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ChildAvatarCircle(student.avatarEmoji, student.avatarColorHex, size = 58.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Halo, ${student.name}!", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                                Text("${student.level.label} • ${student.grade}", style = MaterialTheme.typography.labelLarge, color = PelajariaAmberSoft)
                            }
                            Surface(color = PelajariaAmber, shape = RoundedCornerShape(50)) {
                                Text(
                                    "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0.1f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(50)),
                                color = PelajariaAmber,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Text(
                                "Progres belajarmu: $doneItems dari $totalItems materi & tugas selesai!",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
            }
        }

        // Big Kid-Friendly Action Tiles
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StudentBigTile(
                    title = "Materi",
                    subtitle = "${unreadMaterials.size} Belum Dibaca",
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    bg = PelajariaBlueSoft,
                    accent = PelajariaBlueBright,
                    testTag = "student_tile_materi",
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectTab(1) }

                StudentBigTile(
                    title = "Tugas Saya",
                    subtitle = "${pendingTasks.size} Harus Dikerjakan",
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    bg = PelajariaOrangeSoft,
                    accent = PelajariaOrange,
                    testTag = "student_tile_tugas",
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectTab(2) }

                StudentBigTile(
                    title = "Hasil Nilai",
                    subtitle = "${gradedTasks.size} Sudah Dinilai",
                    icon = Icons.Rounded.EmojiEvents,
                    bg = PelajariaGreenSoft,
                    accent = PelajariaGreen,
                    testTag = "student_tile_hasil",
                    modifier = Modifier.weight(1f)
                ) { viewModel.selectTab(3) }
            }
        }

        // Unread Materials
        item {
            Text("Materi Belum Dibaca (${unreadMaterials.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (unreadMaterials.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Semua Materi Sudah Dibaca!",
                    subtitle = "Hebat! Kamu sudah membaca semua materi dari guru."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    unreadMaterials.forEach { doc ->
                        StudentDocumentCard(
                            doc = doc,
                            isRead = false,
                            submission = null,
                            onClick = { viewModel.navigateTo(SubRoute.StudentLearning(doc.id)) }
                        )
                    }
                }
            }
        }

        // Pending Tasks
        item {
            Text("Tugas yang Harus Dikerjakan (${pendingTasks.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (pendingTasks.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Rounded.Celebration,
                    title = "Tugasmu Sudah Terkirim Semua!",
                    subtitle = "Bagus sekali, tunggu nilai dan catatan dari gurumu."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pendingTasks.forEach { doc ->
                        StudentDocumentCard(
                            doc = doc,
                            isRead = false,
                            submission = doc.submissions[student.id],
                            onClick = { viewModel.navigateTo(SubRoute.StudentLearning(doc.id)) }
                        )
                    }
                }
            }
        }

        // Latest Teacher Grade & Note
        if (gradedTasks.isNotEmpty()) {
            item {
                Text("Nilai & Catatan Guru Terbaru", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                val latest = gradedTasks.first()
                val sub = latest.submissions[student.id]
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = PelajariaGreenSoft)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(latest.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Surface(color = PelajariaGreen, shape = RoundedCornerShape(12.dp)) {
                                Text("Nilai ${sub?.score ?: 0}", color = Color.White, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                        Text("Catatan ${latest.teacherName}: \"${sub?.teacherNote ?: "-"}\"", style = MaterialTheme.typography.bodyMedium, color = InkPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentBigTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    bg: Color,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(24.dp))
            }
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun StudentMaterialsTab(
    student: StudentProfile,
    viewModel: PelajariaViewModel
) {
    val materials = viewModel.studentMaterials(student.id)
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceChildBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Materi dari Guru", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("Baca dokumen PDF, foto materi, atau pelajari materi interaktif", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        if (materials.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    title = "Belum Ada Materi",
                    subtitle = "Materi yang dibagikan gurumu akan tampil di sini."
                )
            }
        } else {
            items(materials, key = { it.id }) { doc ->
                val isRead = doc.readByStudentIds.contains(student.id)
                StudentDocumentCard(
                    doc = doc,
                    isRead = isRead,
                    submission = null,
                    onClick = { viewModel.navigateTo(SubRoute.StudentLearning(doc.id)) }
                )
            }
        }
    }
}

@Composable
fun StudentTasksTab(
    student: StudentProfile,
    viewModel: PelajariaViewModel
) {
    val assignments = viewModel.studentAssignments(student.id)
    val todo = assignments.filter { (it.submissions[student.id]?.status ?: SubmissionStatus.NOT_STARTED) == SubmissionStatus.NOT_STARTED }
    val waiting = assignments.filter { it.submissions[student.id]?.status == SubmissionStatus.WAITING_GRADE }
    val done = assignments.filter { it.submissions[student.id]?.status == SubmissionStatus.GRADED }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceChildBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Tugas Saya", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("Kerjakan soal dari foto, PDF, atau soal digital lalu kirim ke gurumu", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        if (assignments.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    title = "Belum Ada Tugas",
                    subtitle = "Tugas dari gurumu akan tampil di halaman ini."
                )
            }
        } else {
            if (todo.isNotEmpty()) {
                item { Text("Belum Dikerjakan (${todo.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PelajariaOrange) }
                items(todo, key = { it.id }) { doc ->
                    StudentDocumentCard(doc, false, doc.submissions[student.id]) {
                        viewModel.navigateTo(SubRoute.StudentLearning(doc.id))
                    }
                }
            }
            if (waiting.isNotEmpty()) {
                item { Text("Menunggu Penilaian Guru (${waiting.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PelajariaBlueBright) }
                items(waiting, key = { it.id }) { doc ->
                    StudentDocumentCard(doc, false, doc.submissions[student.id]) {
                        viewModel.navigateTo(SubRoute.StudentLearning(doc.id))
                    }
                }
            }
            if (done.isNotEmpty()) {
                item { Text("Sudah Selesai & Dinilai (${done.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PelajariaGreen) }
                items(done, key = { it.id }) { doc ->
                    StudentDocumentCard(doc, false, doc.submissions[student.id]) {
                        viewModel.navigateTo(SubRoute.StudentLearning(doc.id))
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentDocumentCard(
    doc: DocumentItem,
    isRead: Boolean,
    submission: StudentSubmission?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("student_doc_${doc.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            SubjectBannerArt(subject = doc.subject, modifier = Modifier.fillMaxWidth().height(92.dp))
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PresentationModeBadge(doc.presentationMode)
                    if (doc.isAssignment) {
                        SubmissionStatusBadge(submission?.status ?: SubmissionStatus.NOT_STARTED)
                    } else {
                        Surface(
                            color = if (isRead) PelajariaGreenSoft else PelajariaAmberSoft,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (isRead) "Sudah Dibaca ✓" else "Belum Dibaca",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isRead) PelajariaGreen else Color(0xFF9A5B00),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Text(doc.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(doc.description, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${doc.teacherName}" + if (doc.dueDate.isNotBlank()) " • ${doc.dueDate}" else "",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSecondary
                    )
                    Button(onClick = onClick, shape = RoundedCornerShape(14.dp)) {
                        Text(if (doc.isAssignment) "Buka Tugas" else "Buka Materi")
                    }
                }
            }
        }
    }
}

@Composable
fun StudentResultsTab(
    student: StudentProfile,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel
) {
    val assignments = viewModel.studentAssignments(student.id)
    val graded = assignments.filter { it.submissions[student.id]?.status == SubmissionStatus.GRADED }
    val readCount = viewModel.studentMaterials(student.id).count { it.readByStudentIds.contains(student.id) }
    val avg = viewModel.studentAverageScore(student.id)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceChildBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Hasil Belajar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("Lihat nilai dan catatan semangat dari gurumu!", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatMetricCard(
                    title = "Rata-rata Nilai",
                    value = if (avg > 0) "$avg" else "-",
                    icon = Icons.Rounded.EmojiEvents,
                    tint = PelajariaGreen,
                    containerColor = PelajariaGreenSoft,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Tugas Dinilai",
                    value = "${graded.size}",
                    icon = Icons.Rounded.FactCheck,
                    tint = PelajariaBlueBright,
                    containerColor = PelajariaBlueSoft,
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Materi Dibaca",
                    value = "$readCount",
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    tint = PelajariaOrange,
                    containerColor = PelajariaOrangeSoft,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (graded.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.EmojiEvents,
                    title = "Belum Ada Nilai",
                    subtitle = "Setelah guru memeriksa jawabanmu, nilai dan catatan akan tampil di sini."
                )
            }
        } else {
            items(graded, key = { it.id }) { doc ->
                val sub = doc.submissions[student.id]!!
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.subject, style = MaterialTheme.typography.labelMedium, color = PelajariaBlue)
                                Text(doc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Surface(color = PelajariaGreenSoft, shape = RoundedCornerShape(14.dp)) {
                                Text(
                                    "${sub.score ?: 0}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PelajariaGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                        if (sub.teacherNote.isNotBlank()) {
                            Surface(color = PelajariaAmberSoft, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Catatan ${doc.teacherName}: \"${sub.teacherNote}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = InkPrimary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentProfileTab(
    student: StudentProfile,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel
) {
    val parentName = appState.parents.find { it.id == student.parentId }?.name ?: "Orang Tua"
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceChildBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChildAvatarCircle(student.avatarEmoji, student.avatarColorHex, size = 76.dp)
                    Text(student.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text("${student.level.label} • ${student.grade}", style = MaterialTheme.typography.bodyLarge, color = InkSecondary)
                    Text("Terhubung dengan Orang Tua: $parentName", style = MaterialTheme.typography.bodySmall, color = PelajariaBlue)
                }
            }
        }
        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = PelajariaCoral),
                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("logout_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ganti Akun / Keluar", fontWeight = FontWeight.Bold)
            }
        }
    }
}
