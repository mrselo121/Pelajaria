package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.PelajariaViewModel
import com.example.viewmodel.SubRoute

@Composable
fun TeacherDashboardTab(
    teacher: TeacherAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel
) {
    val myDocs = appState.documents.filter { it.teacherId == teacher.id }
    val sharedMaterialsCount = myDocs.count { !it.isAssignment }
    val activeTasksCount = myDocs.count { it.isAssignment }
    val waitingGradeCount = myDocs.sumOf { doc ->
        doc.submissions.values.count { it.status == SubmissionStatus.WAITING_GRADE }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceTeacherBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting Header
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(PelajariaBlueDeep, PelajariaTeal)))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50)) {
                            Text(
                                text = "Guru ${teacher.subject} • ${teacher.classLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Selamat datang, ${teacher.name}!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Unggah foto atau PDF materi, tinjau konversi interaktif, dan beri nilai murid secara langsung.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Primary CTA Button: Tambah Materi atau Soal
        item {
            Button(
                onClick = { viewModel.navigateTo(SubRoute.AddDocument) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("teacher_add_document_cta"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PelajariaOrange)
            ) {
                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Tambah Materi atau Soal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // 4 KPI Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMetricCard(
                        title = "Jumlah Murid",
                        value = "${appState.students.size}",
                        icon = Icons.Rounded.Groups,
                        tint = PelajariaTeal,
                        containerColor = PelajariaTealSoft,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Materi Dibagikan",
                        value = "$sharedMaterialsCount",
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        tint = PelajariaBlueBright,
                        containerColor = PelajariaBlueSoft,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatMetricCard(
                        title = "Tugas Aktif",
                        value = "$activeTasksCount",
                        icon = Icons.AutoMirrored.Rounded.Assignment,
                        tint = PelajariaPurple,
                        containerColor = PelajariaPurpleSoft,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Menunggu Nilai",
                        value = "$waitingGradeCount",
                        icon = Icons.Rounded.RateReview,
                        tint = PelajariaOrange,
                        containerColor = PelajariaOrangeSoft,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Waiting Grading Quick List
        if (waitingGradeCount > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Perlu Penilaian Segera", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { viewModel.selectTab(4) }) { Text("Buka Penilaian") }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    myDocs.forEach { doc ->
                        doc.submissions.values.filter { it.status == SubmissionStatus.WAITING_GRADE }.forEach { sub ->
                            val st = appState.students.find { it.id == sub.studentId }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.navigateTo(SubRoute.TeacherGradingDetail(doc.id, sub.studentId)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (st != null) ChildAvatarCircle(st.avatarEmoji, st.avatarColorHex, size = 42.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${st?.name ?: "Murid"} • ${doc.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Dikirim ${sub.submittedAtLabel}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                    }
                                    FilledTonalButton(onClick = { viewModel.navigateTo(SubRoute.TeacherGradingDetail(doc.id, sub.studentId)) }) {
                                        Text("Nilai")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Student Roster Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daftar Murid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { viewModel.selectTab(3) }) { Text("Lihat Semua") }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                appState.students.forEach { st ->
                    val avg = viewModel.studentAverageScore(st.id)
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ChildAvatarCircle(st.avatarEmoji, st.avatarColorHex, size = 44.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(st.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("${st.level.label} • ${st.grade}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                            Text(
                                text = if (avg > 0) "Rata-rata $avg" else "Aktif",
                                style = MaterialTheme.typography.labelMedium,
                                color = PelajariaTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Latest Activity
        item {
            Text("Aktivitas Terbaru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            if (appState.activityRecords.isEmpty()) {
                EmptyStateCard(
                    icon = Icons.Rounded.History,
                    title = "Belum Ada Aktivitas",
                    subtitle = "Aktivitas pengumpulan dan penilaian tugas akan muncul di sini."
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    appState.activityRecords.take(4).forEach { act ->
                        val stName = appState.students.find { it.id == act.studentId }?.name ?: "Murid"
                        Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("$stName: ${act.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("${act.subject} • ${act.timestampLabel}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                }
                                Surface(color = PelajariaBlueSoft, shape = RoundedCornerShape(50)) {
                                    Text(act.scoreLabel, style = MaterialTheme.typography.labelSmall, color = PelajariaBlue, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
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
fun TeacherMaterialsTab(
    teacher: TeacherAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel
) {
    val materials = appState.documents.filter { it.teacherId == teacher.id && !it.isAssignment }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceTeacherBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Materi Pembelajaran", style = MaterialTheme.typography.headlineMedium)
                    Text("Dokumen bacaan asli & materi interaktif", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                Button(onClick = { viewModel.navigateTo(SubRoute.AddDocument) }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah")
                }
            }
        }
        if (materials.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    title = "Belum Ada Materi",
                    subtitle = "Unggah foto atau PDF materi untuk dibagikan ke murid.",
                    actionLabel = "Tambah Materi atau Soal",
                    onAction = { viewModel.navigateTo(SubRoute.AddDocument) }
                )
            }
        } else {
            items(materials, key = { it.id }) { doc ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.navigateTo(SubRoute.TeacherDocumentPreview(doc.id)) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DocumentFormatBadge(doc.format)
                            PresentationModeBadge(doc.presentationMode)
                        }
                        Text(doc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(doc.description, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${doc.subject} • ${doc.grade}", style = MaterialTheme.typography.labelMedium, color = PelajariaBlue)
                            Text("Dibaca ${doc.readByStudentIds.size}/${doc.targetStudentIds.size} murid", style = MaterialTheme.typography.labelMedium, color = PelajariaTeal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherTasksTab(
    teacher: TeacherAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel
) {
    val tasks = appState.documents.filter { it.teacherId == teacher.id && it.isAssignment }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceTeacherBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tugas & Lembar Soal", style = MaterialTheme.typography.headlineMedium)
                    Text("Foto/PDF soal asli & soal digital hasil konversi", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                }
                Button(onClick = { viewModel.navigateTo(SubRoute.AddDocument) }) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah")
                }
            }
        }
        if (tasks.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    title = "Belum Ada Tugas",
                    subtitle = "Tambahkan foto atau PDF lembar soal untuk dikerjakan murid.",
                    actionLabel = "Tambah Materi atau Soal",
                    onAction = { viewModel.navigateTo(SubRoute.AddDocument) }
                )
            }
        } else {
            items(tasks, key = { it.id }) { doc ->
                val waitingCount = doc.submissions.values.count { it.status == SubmissionStatus.WAITING_GRADE }
                val gradedCount = doc.submissions.values.count { it.status == SubmissionStatus.GRADED }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.navigateTo(SubRoute.TeacherDocumentPreview(doc.id)) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DocumentFormatBadge(doc.format)
                            PresentationModeBadge(doc.presentationMode)
                        }
                        Text(doc.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(doc.description, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        HorizontalDivider(color = OutlineSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tenggat: ${doc.dueDate.ifBlank { "-" }}", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
                            Text(
                                "Menunggu nilai: $waitingCount • Dinilai: $gradedCount",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (waitingCount > 0) PelajariaOrange else PelajariaGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherStudentsTab(
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceTeacherBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Daftar Murid", style = MaterialTheme.typography.headlineMedium)
            Text("Pantau keaktifan membaca materi dan penyelesaian tugas setiap murid", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
        }
        items(appState.students, key = { it.id }) { st ->
            val readCount = appState.documents.count { !it.isAssignment && it.readByStudentIds.contains(st.id) }
            val completedTasks = appState.documents.count { it.isAssignment && it.submissions[st.id]?.status != null && it.submissions[st.id]?.status != SubmissionStatus.NOT_STARTED }
            val avg = viewModel.studentAverageScore(st.id)

            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ChildAvatarCircle(st.avatarEmoji, st.avatarColorHex, size = 52.dp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(st.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${st.level.label} • ${st.grade}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        }
                        Surface(color = PelajariaGreenSoft, shape = RoundedCornerShape(12.dp)) {
                            Text(
                                text = if (avg > 0) "Rata-rata $avg" else "Aktif",
                                style = MaterialTheme.typography.labelLarge,
                                color = PelajariaGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = OutlineSubtle)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Materi Dibaca: $readCount", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        Text("Tugas Dikirim: $completedTasks", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherGradingTab(
    teacher: TeacherAccount,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel
) {
    var filterMode by remember { mutableIntStateOf(0) } // 0 = Menunggu, 1 = Sudah Dinilai
    val myDocs = appState.documents.filter { it.teacherId == teacher.id && it.isAssignment }
    val allSubmissions = myDocs.flatMap { doc ->
        doc.submissions.values.map { sub -> doc to sub }
    }.filter { (_, sub) ->
        if (filterMode == 0) sub.status == SubmissionStatus.WAITING_GRADE else sub.status == SubmissionStatus.GRADED
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceTeacherBg),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Penilaian Manual Guru", style = MaterialTheme.typography.headlineMedium)
            Text("Periksa jawaban teks, foto hasil kerja, dan beri nilai serta catatan", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterMode == 0,
                    onClick = { filterMode = 0 },
                    label = { Text("Menunggu Penilaian") },
                    modifier = Modifier.testTag("grading_filter_waiting")
                )
                FilterChip(
                    selected = filterMode == 1,
                    onClick = { filterMode = 1 },
                    label = { Text("Sudah Dinilai") },
                    modifier = Modifier.testTag("grading_filter_graded")
                )
            }
        }

        if (allSubmissions.isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Rounded.FactCheck,
                    title = if (filterMode == 0) "Tidak Ada Antrean Penilaian" else "Belum Ada Tugas Dinilai",
                    subtitle = if (filterMode == 0) "Semua jawaban murid yang masuk sudah Anda periksa." else "Tugas yang sudah dinilai akan muncul di sini."
                )
            }
        } else {
            items(allSubmissions, key = { "${it.first.id}_${it.second.studentId}" }) { (doc, sub) ->
                val student = appState.students.find { it.id == sub.studentId }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(SubRoute.TeacherGradingDetail(doc.id, sub.studentId)) }
                        .testTag("grade_item_${doc.id}_${sub.studentId}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (student != null) ChildAvatarCircle(student.avatarEmoji, student.avatarColorHex, size = 48.dp)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            SubmissionStatusBadge(sub.status)
                            Text("${student?.name ?: "Murid"} • ${doc.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (sub.status == SubmissionStatus.GRADED) {
                                    "Nilai: ${sub.score} • Dinilai ${sub.gradedAtLabel}"
                                } else {
                                    "Dikirim ${sub.submittedAtLabel}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = InkSecondary
                            )
                        }
                        Button(onClick = { viewModel.navigateTo(SubRoute.TeacherGradingDetail(doc.id, sub.studentId)) }) {
                            Text(if (sub.status == SubmissionStatus.GRADED) "Ubah" else "Periksa")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherGradingDetailScreen(
    documentId: String,
    studentId: String,
    appState: PelajariaAppState,
    viewModel: PelajariaViewModel,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val doc = appState.documents.find { it.id == documentId }
    val student = appState.students.find { it.id == studentId }
    val sub = doc?.submissions?.get(studentId)

    var scoreText by remember(documentId, studentId) { mutableStateOf(sub?.score?.toString() ?: "90") }
    var teacherNote by remember(documentId, studentId) {
        mutableStateOf(sub?.teacherNote?.ifBlank { "Bagus sekali! Terus semangat berlatih ya." } ?: "Bagus sekali! Terus semangat berlatih ya.")
    }
    var scoreError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Periksa Pekerjaan Murid", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SurfaceTeacherBg
    ) { padding ->
        if (doc == null || student == null || sub == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyStateCard(icon = Icons.Rounded.ErrorOutline, title = "Data Tidak Ditemukan", subtitle = "Kembali ke daftar penilaian.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Student & Task Info
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ChildAvatarCircle(student.avatarEmoji, student.avatarColorHex, size = 54.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(student.name, style = MaterialTheme.typography.titleLarge)
                        Text("${student.grade} • ${doc.title}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                        Text("Dikirim: ${sub.submittedAtLabel}", style = MaterialTheme.typography.labelSmall, color = PelajariaBlue)
                    }
                    SubmissionStatusBadge(sub.status)
                }
            }

            // Original Question Document Preview
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dokumen Soal dari Guru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                }
            }

            // Student's Submitted Answers (Text, Digital, Photo)
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Jawaban yang Dikirim Murid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (sub.digitalAnswers.isNotEmpty()) {
                        doc.digitalQuestions.forEach { q ->
                            Surface(color = PelajariaBlueSoft.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(q.prompt, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "Jawaban ${student.name}: ${sub.digitalAnswers[q.id] ?: "(Belum diisi)"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PelajariaBlueDeep
                                    )
                                }
                            }
                        }
                    }

                    if (sub.textAnswer.isNotBlank()) {
                        Surface(color = SurfaceLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Jawaban / Catatan Teks Murid:", style = MaterialTheme.typography.labelMedium, color = InkSecondary)
                                Text(sub.textAnswer, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    if (sub.photoUriString.isNotBlank() || sub.sampleAnswerPhotoKey.isNotBlank()) {
                        Text("Foto Hasil Pekerjaan Murid:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        ZoomableDocumentViewer(
                            uriString = sub.photoUriString,
                            samplePresetKey = sub.sampleAnswerPhotoKey.ifBlank { "handwritten_math_sample" },
                            title = "Jawaban ${student.name}"
                        )
                    }
                }
            }

            // Manual Grading Form
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Penilaian Manual Guru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = scoreText,
                        onValueChange = { scoreText = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Nilai (0 - 100)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("grading_score_input")
                    )
                    OutlinedTextField(
                        value = teacherNote,
                        onValueChange = { teacherNote = it },
                        label = { Text("Catatan Guru untuk Murid & Orang Tua") },
                        modifier = Modifier.fillMaxWidth().testTag("grading_note_input")
                    )
                    if (scoreError != null) {
                        Text(scoreError!!, color = PelajariaCoral, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = {
                            val parsed = scoreText.toIntOrNull()
                            if (parsed == null || parsed !in 0..100) {
                                scoreError = "Masukkan nilai angka antara 0 sampai 100."
                            } else {
                                viewModel.saveManualGrade(doc.id, student.id, parsed, teacherNote)
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp).testTag("save_grading_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan Penilaian", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDocumentPreviewScreen(
    documentId: String,
    appState: PelajariaAppState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val doc = appState.documents.find { it.id == documentId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(doc?.title ?: "Preview Dokumen", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = SurfaceTeacherBg
    ) { padding ->
        if (doc == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DocumentFormatBadge(doc.format)
                        PresentationModeBadge(doc.presentationMode)
                    }
                    Text(doc.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(doc.description, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                }
            }
            if (doc.format == DocumentFormat.IMAGE) {
                ZoomableDocumentViewer(doc.sourceUriString, doc.samplePresetKey, doc.title)
            } else {
                PdfDocumentViewer(doc.sourceUriString, doc.pdfSimulatedPages, doc.title)
            }
            if (doc.interactiveSections.isNotEmpty()) {
                Text("Bagian Interaktif (${doc.interactiveSections.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                doc.interactiveSections.forEach { sec ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${sec.visualEmoji} ${sec.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(sec.explanation, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            if (doc.digitalQuestions.isNotEmpty()) {
                Text("Soal Digital (${doc.digitalQuestions.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                doc.digitalQuestions.forEach { q ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(q.prompt, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            if (q.options.isNotEmpty()) {
                                Text("Opsi: ${q.options.joinToString(" | ")}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherProfileTab(
    teacher: TeacherAccount,
    viewModel: PelajariaViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SurfaceTeacherBg),
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
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(PelajariaTealSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(teacher.name.take(2).uppercase(), style = MaterialTheme.typography.headlineMedium, color = PelajariaTeal)
                    }
                    Text(teacher.name, style = MaterialTheme.typography.headlineMedium)
                    Text(teacher.email, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    Surface(color = PelajariaTealSoft, shape = RoundedCornerShape(50)) {
                        Text(
                            "Guru ${teacher.subject} • ${teacher.classLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = PelajariaTeal,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
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
                        Text("Pulihkan Data Simulasi Demo")
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
}
