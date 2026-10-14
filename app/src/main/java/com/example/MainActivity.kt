package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.local.database.StudyDatabase
import com.example.data.repository.StudyRepository
import com.example.ui.blocker.BlockerScreen
import com.example.ui.blocker.BlockerViewModel
import com.example.ui.blocker.BlockerViewModelFactory
import com.example.ui.history.HistoryScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.history.HistoryViewModelFactory
import com.example.ui.navigation.AppDestination
import com.example.ui.pomodoro.PomodoroScreen
import com.example.ui.pomodoro.PomodoroViewModel
import com.example.ui.pomodoro.PomodoroViewModelFactory
import com.example.ui.stats.StatsHistoryScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.stats.StatsViewModel
import com.example.ui.stats.StatsViewModelFactory
import com.example.ui.subjects.SubjectsScreen
import com.example.ui.subjects.SubjectsViewModel
import com.example.ui.subjects.SubjectsViewModelFactory
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PomodoroRed
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = StudyDatabase.getInstance(applicationContext)
        com.example.service.FocusBlockManager.init(applicationContext)
        val repository = StudyRepository(
            subjectDao = database.subjectDao(),
            studySessionDao = database.studySessionDao(),
            topicDao = database.topicDao(),
            blockedAppDao = database.blockedAppDao(),
            blockedWebsiteDao = database.blockedWebsiteDao(),
            blockSettingsDao = database.blockSettingsDao(),
            studyCycleDao = database.studyCycleDao(),
            studyScheduleDao = database.studyScheduleDao()
        )

        val pomodoroViewModel by viewModels<PomodoroViewModel> {
            PomodoroViewModelFactory(repository, application)
        }
        val blockerViewModel by viewModels<BlockerViewModel> {
            BlockerViewModelFactory(application, repository)
        }
        val historyViewModel by viewModels<HistoryViewModel> {
            HistoryViewModelFactory(repository)
        }
        val statsViewModel by viewModels<StatsViewModel> {
            StatsViewModelFactory(repository)
        }
        val subjectsViewModel by viewModels<SubjectsViewModel> {
            SubjectsViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                MainContent(
                    pomodoroViewModel = pomodoroViewModel,
                    blockerViewModel = blockerViewModel,
                    historyViewModel = historyViewModel,
                    statsViewModel = statsViewModel,
                    subjectsViewModel = subjectsViewModel
                )
            }
        }
    }
}

@Composable
fun MainContent(
    pomodoroViewModel: PomodoroViewModel,
    blockerViewModel: BlockerViewModel,
    historyViewModel: HistoryViewModel,
    statsViewModel: StatsViewModel,
    subjectsViewModel: SubjectsViewModel
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.POMODORO) }

    // Suporte a retorno para a tela inicial ao pressionar Voltar
    BackHandler(enabled = currentDestination != AppDestination.POMODORO) {
        currentDestination = AppDestination.POMODORO
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary
            ) {
                AppDestination.values().forEach { destination ->
                    val selected = currentDestination == destination
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_item_${destination.route}"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = PomodoroRed,
                            indicatorColor = PomodoroRed,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (currentDestination) {
            AppDestination.POMODORO -> PomodoroScreen(viewModel = pomodoroViewModel, modifier = modifier)
            AppDestination.SUBJECTS -> SubjectsScreen(viewModel = subjectsViewModel, modifier = modifier)
            AppDestination.STATS_HISTORY -> StatsHistoryScreen(
                statsViewModel = statsViewModel,
                historyViewModel = historyViewModel,
                modifier = modifier
            )
            AppDestination.BLOCKER -> BlockerScreen(viewModel = blockerViewModel, modifier = modifier)
        }
    }
}
