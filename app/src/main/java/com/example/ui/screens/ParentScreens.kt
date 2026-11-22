package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PelajariaViewModel

@Composable
fun ParentHomeTab(
    parent: ParentAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel,
    selectedStudentId: String?
) {
    val myChildren = appState.students.filter { it.parentId == parent.id }
    val activeChild = myChildren.find { it.id == selectedStudentId } ?: myChildren.firstOrNull()
    val materials = activeChild?.let { viewModel.studentMaterials(it.id) } ?: emptyList()
    val assignments = activeChild?.let { viewModel.studentAssignments(it.id) } ?: emptyList()
    val gradedList = assignments.filter { it.submissions[activeChild?.id]?.status == SubmissionStatus.GRADED }
    val pendingCount = assignments.count {
        (it.submissions[activeChild?.id]?.status ?: SubmissionStatus.NOT_STARTED) != SubmissionStatus.GRADED
    }
    val avgScore = activeChild?.let { viewModel.studentAverageScore(it.id) } ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(PelajariaBlue, PelajariaBlueBright)))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(parent.name.take(1).uppercase(), style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Selamat datang kembali,", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            Text(parent.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Pendamping Belajar • Pantau Aktivitas Anak", style = MaterialTheme.typography.labelMedium, color = PelajariaAmberSoft)
                        }
                    }
                }
            }
        }

        if (myChildren.size > 1) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(myChildren, key = { it.id }) { st ->
                        FilterChip(
                            selected = st.id == activeChild?.id,
                            onClick = { viewModel.selectParentStudent(st.id) },
                            label = { Text("${st.avatarEmoji} ${st.name}") }
                        )
                    }
                }
            }
        }

        if (activeChild != null) {
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ChildAvatarCircle(activeChild.avatarEmoji, activeChild.avatarColorHex, size = 54.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activeChild.name, style = MaterialTheme.typography.titleLarge)
                            Text("${activeChild.level.label} • ${activeChild.grade}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        Surface(color = PelajariaBlueSoft, shape = RoundedCornerShape(50)) {
                            Text(
                                "Dipantau",
                                style = MaterialTheme.typography.labelMedium,
                                color = PelajariaBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMetricCard(
                        title = "Materi Guru",
                        value = "${materials.size}",
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        tint = PelajariaBlueBright,
                        containerColor = PelajariaBlueSoft,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Tugas Aktif",
                        value = "$pendingCount",
                        icon = Icons.AutoMirrored.Rounded.Assignment,
                        tint = PelajariaOrange,
                        containerColor = PelajariaOrangeSoft,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Rata-rata",
                        value = if (avgScore > 0) "$avgScore" else "-",
                        icon = Icons.Rounded.Stars,
                        tint = PelajariaGreen,
                        containerColor = PelajariaGreenSoft,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Simple Progress Chart
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Grafik Perkembangan Nilai ${activeChild.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val ordered = gradedList.reversed()
                        ScoreTrendChart(
                            labels = ordered.map { it.subject },
                            scores = ordered.map { it.submissions[activeChild.id]?.score ?: 0 }
                        )
                    }
                }
            }

            // Recent Teacher Notes
            item {
                Text("Nilai & Catatan Guru Terbaru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (gradedList.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Rounded.FactCheck,
                        title = "Belum Ada Nilai",
                        subtitle = "Nilai dan catatan guru akan muncul setelah tugas anak diperiksa."
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        gradedList.take(3).forEach { doc ->
                            val sub = doc.submissions[activeChild.id]!!
                            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(doc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Surface(color = PelajariaGreenSoft, shape = RoundedCornerShape(10.dp)) {
                                            Text("Nilai ${sub.score ?: 0}", color = PelajariaGreen, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                        }
                                    }
                                    Text("${doc.subject} • ${doc.teacherName}", style = MaterialTheme.typography.labelSmall, color = InkSecondary)
                                    if (sub.teacherNote.isNotBlank()) {
                                        Surface(color = PelajariaAmberSoft, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            Text("Catatan Guru: \"${sub.teacherNote}\"", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentMonitoringTab(
    parent: ParentAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel,
    selectedStudentId: String?
) {
    val myChildren = appState.students.filter { it.parentId == parent.id }
    val activeChild = myChildren.find { it.id == selectedStudentId } ?: myChildren.firstOrNull()
    val materials = activeChild?.let { viewModel.studentMaterials(it.id) } ?: emptyList()
    val assignments = activeChild?.let { viewModel.studentAssignments(it.id) } ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Materi & Tugas Anak", style = MaterialTheme.typography.headlineMedium)
            Text("Daftar pembelajaran yang diberikan oleh Guru untuk ${activeChild?.name ?: "anak"}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }

        item {
            Text("Tugas dari Guru (${assignments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        if (assignments.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    title = "Belum Ada Tugas",
                    subtitle = "Tugas dari guru akan tampil di sini untuk Anda pantau."
                )
            }
        } else {
            items(assignments, key = { it.id }) { doc ->
                val sub = activeChild?.let { doc.submissions[it.id] }
                val status = sub?.status ?: SubmissionStatus.NOT_STARTED
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PresentationModeBadge(doc.presentationMode)
                            SubmissionStatusBadge(status)
                        }
                        Text(doc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${doc.subject} • ${doc.teacherName} • Tenggat: ${doc.dueDate.ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                        if (sub?.status == SubmissionStatus.GRADED) {
                            Surface(color = PelajariaGreenSoft, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Nilai Guru: ${sub.score ?: 0}", style = MaterialTheme.typography.labelLarge, color = PelajariaGreen, fontWeight = FontWeight.Bold)
                                    if (sub.teacherNote.isNotBlank()) {
                                        Text("Catatan: ${sub.teacherNote}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("Materi Belajar (${materials.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(materials, key = { it.id }) { doc ->
            val isRead = activeChild != null && doc.readByStudentIds.contains(activeChild.id)
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        PresentationModeBadge(doc.presentationMode)
                        Text(doc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${doc.subject} • ${doc.teacherName}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                    Surface(
                        color = if (isRead) PelajariaGreenSoft else PelajariaAmberSoft,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (isRead) "Sudah Dibaca" else "Belum Dibaca",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRead) PelajariaGreen else Color(0xFF9A5B00),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParentProgressSummaryTab(
    parent: ParentAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel,
    selectedStudentId: String?
) {
    val myChildren = appState.students.filter { it.parentId == parent.id }
    val activeChild = myChildren.find { it.id == selectedStudentId } ?: myChildren.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Ringkasan Perkembangan", style = MaterialTheme.typography.headlineMedium)
            Text("Pantau grafik nilai, catatan guru, dan riwayat aktivitas belajar", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        if (activeChild != null) {
            val gradedDocs = viewModel.studentAssignments(activeChild.id)
                .filter { it.submissions[activeChild.id]?.status == SubmissionStatus.GRADED }
            val logs = appState.activityRecords.filter { it.studentId == activeChild.id }
            val readCount = viewModel.studentMaterials(activeChild.id).count { it.readByStudentIds.contains(activeChild.id) }
            val avg = viewModel.studentAverageScore(activeChild.id)

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMetricCard("Rata-rata", if (avg > 0) "$avg" else "-", Icons.Rounded.TrendingUp, PelajariaGreen, PelajariaGreenSoft, Modifier.weight(1f))
                    StatMetricCard("Tugas Dinilai", "${gradedDocs.size}", Icons.Rounded.FactCheck, PelajariaBlueBright, PelajariaBlueSoft, Modifier.weight(1f))
                    StatMetricCard("Materi Dibaca", "$readCount", Icons.AutoMirrored.Rounded.MenuBook, PelajariaOrange, PelajariaOrangeSoft, Modifier.weight(1f))
                }
            }
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Grafik Nilai Anak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val ordered = gradedDocs.reversed()
                        ScoreTrendChart(
                            labels = ordered.map { it.subject },
                            scores = ordered.map { it.submissions[activeChild.id]?.score ?: 0 }
                        )
                    }
                }
            }
            item {
                Text("Riwayat Aktivitas Belajar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (logs.isEmpty()) {
                item {
                    EmptyStateCard(icon = Icons.Rounded.History, title = "Belum Ada Aktivitas", subtitle = "Aktivitas belajar anak akan tercatat di sini.")
                }
            } else {
                items(logs, key = { it.id }) { log ->
                    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(log.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("${log.subject} • ${log.timestampLabel}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                            Surface(color = PelajariaBlueSoft, shape = RoundedCornerShape(50)) {
                                Text(log.scoreLabel, style = MaterialTheme.typography.labelSmall, color = PelajariaBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentProfileTab(
    parent: ParentAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel
) {
    val myChildren = appState.students.filter { it.parentId == parent.id }
    var editingStudent by remember { mutableStateOf<StudentProfile?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceLight),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(PelajariaBlueSoft), contentAlignment = Alignment.Center) {
                        Text(parent.name.take(1).uppercase(), style = MaterialTheme.typography.headlineLarge, color = PelajariaBlue)
                    }
                    Text(parent.name, style = MaterialTheme.typography.headlineMedium)
                    Text(parent.email, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    Surface(color = PelajariaBlueSoft, shape = RoundedCornerShape(50)) {
                        Text(
                            "Orang Tua Pendamping • ${myChildren.size} Anak",
                            style = MaterialTheme.typography.labelMedium,
                            color = PelajariaBlue,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Kelola Profil Anak", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = {
                    editingStudent = null
                    showFormDialog = true
                }) {
                    Text("+ Tambah Anak")
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                myChildren.forEach { st ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ChildAvatarCircle(st.avatarEmoji, st.avatarColorHex, size = 46.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(st.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${st.level.label} • ${st.grade}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                            IconButton(onClick = {
                                editingStudent = st
                                showFormDialog = true
                            }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Ubah Profil Anak")
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = { viewModel.resetDemoData() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pulihkan Data Demo Awal")
                    }
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = PelajariaCoral),
                        modifier = Modifier.fillMaxWidth().testTag("logout_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Keluar / Ganti Peran")
                    }
                }
            }
        }
    }

    if (showFormDialog) {
        var name by remember { mutableStateOf(editingStudent?.name ?: "") }
        var level by remember { mutableStateOf(editingStudent?.level ?: EducationLevel.SD) }
        var grade by remember { mutableStateOf(editingStudent?.grade ?: "2 SD") }
        var emoji by remember { mutableStateOf(editingStudent?.avatarEmoji ?: "🦊") }
        var err by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showFormDialog = false },
            title = { Text(if (editingStudent == null) "Tambah Profil Anak" else "Ubah Profil Anak") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Anak") }, singleLine = true, modifier = Modifier.fillMaxWidth())
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
                    OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Kelas") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("🦊", "🐼", "🦁", "🐰").forEach { e ->
                            Surface(
                                modifier = Modifier.size(38.dp).clip(CircleShape).clickable { emoji = e },
                                color = if (emoji == e) PelajariaBlueSoft else SurfaceLight
                            ) { Box(contentAlignment = Alignment.Center) { Text(e) } }
                        }
                    }
                    if (err != null) Text(err!!, color = PelajariaCoral, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val res = viewModel.saveStudentProfileByParent(editingStudent?.id, name, level, grade, emoji, 0xFFFB8C00)
                    if (res == null) showFormDialog = false else err = res
                }) { Text("Simpan") }
            },
            dismissButton = { TextButton(onClick = { showFormDialog = false }) { Text("Batal") } }
        )
    }
}
