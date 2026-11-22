package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.UserRole
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AppRootDestination
import com.example.viewmodel.PelajariaUiState
import com.example.viewmodel.PelajariaViewModel
import com.example.viewmodel.SubRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PelajariaTheme {
                PelajariaRootApp()
            }
        }
    }
}

private data class NavTabItem(
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun PelajariaRootApp(
    viewModel: PelajariaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.feedbackMessage) {
        val msg = uiState.feedbackMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            viewModel.clearFeedback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.root) {
            AppRootDestination.SPLASH -> {
                SplashScreen(onStartClick = { viewModel.startFromSplash() })
            }
            AppRootDestination.AUTH -> {
                AuthScreen(viewModel = viewModel, appState = uiState.appState)
            }
            AppRootDestination.MAIN -> {
                val activeSubRoute = uiState.subRouteStack.lastOrNull()
                if (activeSubRoute != null) {
                    RenderSubRoute(route = activeSubRoute, uiState = uiState, viewModel = viewModel)
                } else {
                    MainRoleShell(uiState = uiState, viewModel = viewModel)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 76.dp, start = 16.dp, end = 16.dp)
        )
    }
}

@Composable
private fun MainRoleShell(
    uiState: PelajariaUiState,
    viewModel: PelajariaViewModel
) {
    val session = uiState.session ?: return
    val tabs = remember(session.role) {
        when (session.role) {
            UserRole.TEACHER -> listOf(
                NavTabItem("Dashboard", Icons.Rounded.Dashboard, "nav_teacher_dashboard"),
                NavTabItem("Materi", Icons.AutoMirrored.Rounded.MenuBook, "nav_teacher_materials"),
                NavTabItem("Tugas", Icons.AutoMirrored.Rounded.Assignment, "nav_teacher_tasks"),
                NavTabItem("Murid", Icons.Rounded.Groups, "nav_teacher_students"),
                NavTabItem("Penilaian", Icons.Rounded.FactCheck, "nav_teacher_grading"),
                NavTabItem("Profil", Icons.Rounded.Person, "nav_teacher_profile")
            )
            UserRole.STUDENT -> listOf(
                NavTabItem("Beranda", Icons.Rounded.Home, "nav_student_home"),
                NavTabItem("Materi", Icons.AutoMirrored.Rounded.MenuBook, "nav_student_materials"),
                NavTabItem("Tugas Saya", Icons.AutoMirrored.Rounded.Assignment, "nav_student_tasks"),
                NavTabItem("Hasil Belajar", Icons.Rounded.EmojiEvents, "nav_student_results"),
                NavTabItem("Profil", Icons.Rounded.Person, "nav_student_profile")
            )
            UserRole.PARENT -> listOf(
                NavTabItem("Beranda", Icons.Rounded.Home, "nav_parent_home"),
                NavTabItem("Materi & Tugas", Icons.AutoMirrored.Rounded.MenuBook, "nav_parent_monitoring"),
                NavTabItem("Perkembangan", Icons.Rounded.Insights, "nav_parent_progress"),
                NavTabItem("Profil", Icons.Rounded.Person, "nav_parent_profile")
            )
        }
    }

    val accent = when (session.role) {
        UserRole.TEACHER -> PelajariaTeal
        UserRole.STUDENT -> PelajariaOrange
        UserRole.PARENT -> PelajariaBlueBright
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = Color.White,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    tabs.forEachIndexed { idx, item ->
                        NavigationRailItem(
                            selected = uiState.selectedTab == idx,
                            onClick = { viewModel.selectTab(idx) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, fontWeight = FontWeight.SemiBold, maxLines = 1) },
                            colors = NavigationRailItemDefaults.colors(selectedIconColor = accent, selectedTextColor = accent),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    Box(modifier = Modifier.fillMaxSize().widthIn(max = 760.dp).align(Alignment.TopCenter)) {
                        RoleTabContent(uiState, viewModel)
                    }
                }
            }
        } else {
            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    Surface(
                        color = Color.White,
                        shadowElevation = 6.dp,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEachIndexed { idx, item ->
                                val selected = uiState.selectedTab == idx
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectTab(idx) }
                                        .padding(vertical = 4.dp, horizontal = 2.dp)
                                        .testTag(item.testTag),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (selected) accent.copy(alpha = 0.14f) else Color.Transparent)
                                            .padding(horizontal = 10.dp, vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            tint = if (selected) accent else InkSecondary,
                                            modifier = Modifier.size(21.dp)
                                        )
                                    }
                                    Text(
                                        text = item.label,
                                        fontSize = 10.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) accent else InkSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    RoleTabContent(uiState, viewModel)
                }
            }
        }
    }
}

@Composable
private fun RoleTabContent(
    uiState: PelajariaUiState,
    viewModel: PelajariaViewModel
) {
    val session = uiState.session ?: return
    val state = uiState.appState

    when (session.role) {
        UserRole.TEACHER -> {
            val teacher = state.teachers.find { it.id == session.userId } ?: return
            when (uiState.selectedTab) {
                0 -> TeacherDashboardTab(teacher, state, viewModel)
                1 -> TeacherMaterialsTab(teacher, state, viewModel)
                2 -> TeacherTasksTab(teacher, state, viewModel)
                3 -> TeacherStudentsTab(state, viewModel)
                4 -> TeacherGradingTab(teacher, state, viewModel)
                else -> TeacherProfileTab(teacher, viewModel)
            }
        }
        UserRole.STUDENT -> {
            val student = state.students.find { it.id == session.userId } ?: return
            when (uiState.selectedTab) {
                0 -> StudentHomeTab(student, viewModel)
                1 -> StudentMaterialsTab(student, viewModel)
                2 -> StudentTasksTab(student, viewModel)
                3 -> StudentResultsTab(student, state, viewModel)
                else -> StudentProfileTab(student, state, viewModel)
            }
        }
        UserRole.PARENT -> {
            val parent = state.parents.find { it.id == session.userId } ?: return
            when (uiState.selectedTab) {
                0 -> ParentHomeTab(parent, state, viewModel, uiState.selectedParentStudentId)
                1 -> ParentMonitoringTab(parent, state, viewModel, uiState.selectedParentStudentId)
                2 -> ParentProgressSummaryTab(parent, state, viewModel, uiState.selectedParentStudentId)
                else -> ParentProfileTab(parent, state, viewModel)
            }
        }
    }
}

@Composable
private fun RenderSubRoute(
    route: SubRoute,
    uiState: PelajariaUiState,
    viewModel: PelajariaViewModel
) {
    val session = uiState.session
    val state = uiState.appState
    val onBack = { viewModel.popSubRoute() }

    when (route) {
        SubRoute.AddDocument -> {
            val teacher = state.teachers.find { it.id == session?.userId } ?: state.teachers.first()
            AddDocumentScreen(
                teacher = teacher,
                appState = state,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        is SubRoute.TeacherGradingDetail -> {
            TeacherGradingDetailScreen(
                documentId = route.documentId,
                studentId = route.studentId,
                appState = state,
                viewModel = viewModel,
                onBack = onBack
            )
        }
        is SubRoute.TeacherDocumentPreview -> {
            TeacherDocumentPreviewScreen(
                documentId = route.documentId,
                appState = state,
                onBack = onBack
            )
        }
        is SubRoute.StudentLearning -> {
            val student = state.students.find { it.id == session?.userId } ?: state.students.first()
            StudentLearningScreen(
                documentId = route.documentId,
                student = student,
                appState = state,
                viewModel = viewModel,
                onBack = onBack
            )
        }
    }
}
