package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.PelajariaAppState
import com.example.model.UserRole
import com.example.ui.components.ChildAvatarCircle
import com.example.ui.components.PelajariaLogoBadge
import com.example.ui.theme.*
import com.example.viewmodel.PelajariaViewModel

@Composable
fun SplashScreen(
    onStartClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFEFF6FF), Color.White, Color(0xFFFFFBF0))
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PelajariaLogoBadge(size = 56.dp)
                    Column {
                        Text(
                            text = "Pelajaria",
                            style = MaterialTheme.typography.displayLarge,
                            color = PelajariaBlueDeep
                        )
                        Surface(color = PelajariaAmberSoft, shape = RoundedCornerShape(50)) {
                            Text(
                                text = "Teman Belajar Anak",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF9A5B00),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_splash_kids),
                    contentDescription = "Ilustrasi belajar anak bersama Pelajaria",
                    modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Belajar Lebih Mudah,\nTumbuh Lebih Pintar.",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = InkPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Guru membagikan foto & PDF pembelajaran, murid belajar secara interaktif, dan orang tua memantau perkembangan dengan tenang.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary,
                    textAlign = TextAlign.Center
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    RolePillChip("Guru", Icons.Rounded.School, PelajariaTeal)
                    RolePillChip("Murid", Icons.Rounded.ChildCare, PelajariaOrange)
                    RolePillChip("Orang Tua", Icons.Rounded.FamilyRestroom, PelajariaBlue)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onStartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("splash_start_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PelajariaBlueBright)
                ) {
                    Text("Mulai Sekarang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun RolePillChip(label: String, icon: ImageVector, color: Color) {
    Surface(color = color.copy(alpha = 0.10f), shape = RoundedCornerShape(50)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AuthScreen(
    viewModel: PelajariaViewModel,
    appState: PelajariaAppState
) {
    var selectedRole by remember { mutableStateOf(UserRole.TEACHER) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("siti@pelajaria.test") }
    var passwordInput by remember { mutableStateOf("pelajaria") }
    var teacherSubjectInput by remember { mutableStateOf("Matematika") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PelajariaLogoBadge(size = 46.dp)
                Column {
                    Text("Masuk ke Pelajaria", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Teman Belajar Anak • TK & SD",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text("Pilih Peran Pengguna", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UserRole.entries.forEach { role ->
                    val selected = selectedRole == role
                    val (icon, accent) = when (role) {
                        UserRole.TEACHER -> Icons.Rounded.School to PelajariaTeal
                        UserRole.STUDENT -> Icons.Rounded.Face to PelajariaOrange
                        UserRole.PARENT -> Icons.Rounded.FamilyRestroom to PelajariaBlueBright
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                selectedRole = role
                                errorMessage = null
                                isRegisterMode = false
                                emailInput = when (role) {
                                    UserRole.TEACHER -> "siti@pelajaria.test"
                                    UserRole.PARENT -> "parent@pelajaria.test"
                                    UserRole.STUDENT -> ""
                                }
                                passwordInput = "pelajaria"
                            }
                            .testTag("role_tab_${role.name.lowercase()}"),
                        color = if (selected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) accent else OutlineSubtle
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(icon, contentDescription = role.label, tint = if (selected) accent else InkSecondary)
                            Text(
                                text = role.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) accent else InkPrimary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (selectedRole == UserRole.STUDENT) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Pilih Profil Murid", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Ketuk namamu untuk langsung belajar dan mengerjakan tugas dari Guru.",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                        appState.students.forEach { student ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { viewModel.loginAsDemo(UserRole.STUDENT, student.id) }
                                    .testTag("student_login_${student.id}"),
                                color = PelajariaBlueSoft.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ChildAvatarCircle(student.avatarEmoji, student.avatarColorHex, size = 52.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${student.level.label} • ${student.grade}", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                                    }
                                    FilledTonalButton(onClick = { viewModel.loginAsDemo(UserRole.STUDENT, student.id) }) {
                                        Text("Belajar")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isRegisterMode) "Daftar ${selectedRole.label}" else "Masuk ${selectedRole.label}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            TextButton(
                                onClick = {
                                    isRegisterMode = !isRegisterMode
                                    errorMessage = null
                                    if (isRegisterMode) {
                                        nameInput = ""
                                        emailInput = ""
                                        passwordInput = ""
                                    } else {
                                        emailInput = if (selectedRole == UserRole.TEACHER) "siti@pelajaria.test" else "parent@pelajaria.test"
                                        passwordInput = "pelajaria"
                                    }
                                }
                            ) {
                                Text(if (isRegisterMode) "Sudah punya akun?" else "Buat Akun Baru")
                            }
                        }

                        if (isRegisterMode) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Nama Lengkap") },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (selectedRole == UserRole.TEACHER) {
                                OutlinedTextField(
                                    value = teacherSubjectInput,
                                    onValueChange = { teacherSubjectInput = it },
                                    label = { Text("Mata Pelajaran") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email") },
                            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_email_input")
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Kata Sandi") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = "Tampilkan sandi"
                                    )
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                        )

                        if (errorMessage != null) {
                            Surface(color = PelajariaCoralSoft, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = PelajariaCoral)
                                    Text(errorMessage!!, style = MaterialTheme.typography.bodySmall, color = PelajariaCoral)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                errorMessage = if (isRegisterMode) {
                                    if (selectedRole == UserRole.TEACHER) {
                                        viewModel.registerTeacher(nameInput, emailInput, passwordInput, teacherSubjectInput)
                                    } else {
                                        viewModel.registerParent(nameInput, emailInput, passwordInput)
                                    }
                                } else {
                                    viewModel.loginWithEmail(selectedRole, emailInput, passwordInput)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("auth_submit_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (isRegisterMode) "Daftar Sekarang" else "Masuk", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Demo Accounts
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PelajariaAmberSoft.copy(alpha = 0.65f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = PelajariaOrange)
                        Text("Akses Cepat Akun Demo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Uji alur utama Guru → Murid → Penilaian Manual → Pantauan Orang Tua:",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                    DemoAccountRow(
                        title = "Ibu Siti • Guru Matematika (Utama)",
                        subtitle = "siti@pelajaria.test (sandi: pelajaria)",
                        accent = PelajariaTeal,
                        testTag = "demo_teacher_siti",
                        onClick = { viewModel.loginAsDemo(UserRole.TEACHER, "teacher_siti") }
                    )
                    DemoAccountRow(
                        title = "Alya • Murid Kelas 2 SD (Utama)",
                        subtitle = "Baca PDF, zoom foto, materi interaktif, kirim jawaban",
                        accent = PelajariaOrange,
                        testTag = "demo_student_alya",
                        onClick = { viewModel.loginAsDemo(UserRole.STUDENT, "student_alya") }
                    )
                    DemoAccountRow(
                        title = "Budi Santoso • Orang Tua Alya (Pendamping)",
                        subtitle = "parent@pelajaria.test (sandi: pelajaria)",
                        accent = PelajariaBlueBright,
                        testTag = "demo_parent_budi",
                        onClick = { viewModel.loginAsDemo(UserRole.PARENT, "parent_budi") }
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoAccountRow(
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
        color = Color.White,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accent))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = "Masuk", tint = accent, modifier = Modifier.size(18.dp))
        }
    }
}
