package com.example.ui.pomodoro

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.data.local.dao.CycleItemWithSubject
import com.example.data.local.dao.ScheduleWithSubject
import com.example.data.local.entity.StudyAgendaEntity
import com.example.data.local.entity.StudyCycleEntity
import com.example.data.repository.ReviewStatus
import com.example.data.repository.TopicReviewItem
import com.example.data.repository.TopicPendingPomodoroItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.domain.model.PomodoroPhase
import com.example.domain.model.StudyType
import com.example.domain.model.TimerState
import com.example.domain.model.TimerTickState
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BreakGreen
import com.example.ui.theme.LongBreakBlue
import com.example.ui.theme.PomodoroRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    modifier: Modifier = Modifier
) {
    val timerTick by viewModel.timerState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    val cycles by viewModel.studyCycles.collectAsStateWithLifecycle()
    val cycleItems by viewModel.cycleItems.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val agendas by viewModel.agendas.collectAsStateWithLifecycle()
    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val scheduledReviews by viewModel.scheduledReviews.collectAsStateWithLifecycle()
    val topicsAwaitingPomodoros by viewModel.topicsAwaitingPomodoros.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val phaseColor by animateColorAsState(
        targetValue = when (timerTick.phase) {
            PomodoroPhase.FOCUS -> PomodoroRed
            PomodoroPhase.SHORT_BREAK -> BreakGreen
            PomodoroPhase.LONG_BREAK -> LongBreakBlue
        },
        animationSpec = tween(durationMillis = 400),
        label = "phaseColor"
    )

    val phases = remember { listOf(PomodoroPhase.FOCUS, PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK) }
    val currentPhaseIndex = remember(timerTick.phase) {
        phases.indexOf(timerTick.phase).coerceAtLeast(0)
    }
    val pagerState = rememberPagerState(initialPage = currentPhaseIndex, pageCount = { phases.size })
    val coroutineScope = rememberCoroutineScope()

    // Sincronizar mudança de página por deslize com o ViewModel
    LaunchedEffect(pagerState.currentPage) {
        val pagePhase = phases[pagerState.currentPage]
        if (timerTick.phase != pagePhase) {
            viewModel.selectPhase(pagePhase)
        }
    }

    // Sincronizar mudança externa de fase com o Pager
    LaunchedEffect(timerTick.phase) {
        val targetIndex = phases.indexOf(timerTick.phase)
        if (targetIndex >= 0 && pagerState.currentPage != targetIndex) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Barra Superior: Indicador de Bloqueio e Configurações
        val blockState by com.example.service.FocusBlockManager.runtimeState.collectAsState()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (blockState.isFocusActive) PomodoroRed.copy(alpha = 0.15f) else SurfaceCard)
                    .border(
                        1.dp,
                        if (blockState.isFocusActive) PomodoroRed else SurfaceCardBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("pomodoro_block_status_chip")
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Status do Bloqueio",
                    tint = if (blockState.isFocusActive) PomodoroRed else BreakGreen,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (blockState.isFocusActive) "Bloqueio ativo" else "Protegido",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (blockState.isFocusActive) PomodoroRed else TextSecondary
                )
            }

            IconButton(
                onClick = { viewModel.toggleSettingsDialog(true) },
                modifier = Modifier.testTag("settings_button").size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurar",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fases do Pomodoro (Alternância por toque nos botões ou deslize da tela)
        Row(
            modifier = Modifier
                .background(SurfaceCard, RoundedCornerShape(20.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            phases.forEachIndexed { index, phase ->
                val isCurrent = timerTick.phase == phase
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isCurrent) phaseColor else Color.Transparent)
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                            viewModel.selectPhase(phase)
                        }
                        .testTag("mode_button_${phase.name.lowercase()}")
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phase.label,
                        color = if (isCurrent) Color.White else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card de Metadados da Sessão Ativa
        if (uiState.isSessionConfigured && timerTick.phase == PomodoroPhase.FOCUS) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (timerTick.state != TimerState.RUNNING) {
                            viewModel.openPreSessionDialog()
                        }
                    },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.activeTopic,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val subjectColor = uiState.activeSubject?.colorHex?.let {
                                try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { PomodoroRed }
                            } ?: PomodoroRed

                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(subjectColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = uiState.activeSubject?.name ?: "Geral",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "• ",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = uiState.activeStudyType.icon,
                                    contentDescription = null,
                                    tint = phaseColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = uiState.activeStudyType.label,
                                    color = phaseColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    if (timerTick.state != TimerState.RUNNING) {
                        IconButton(
                            onClick = { viewModel.openPreSessionDialog() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Navegação por Deslize Horizontal entre os Modos (Foco, Pausa Curta, Pausa Longa)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pomodoro_phase_pager"),
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            val pagePhase = phases[page]
            val pageColor = when (pagePhase) {
                PomodoroPhase.FOCUS -> PomodoroRed
                PomodoroPhase.SHORT_BREAK -> BreakGreen
                PomodoroPhase.LONG_BREAK -> LongBreakBlue
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularTimerDisplay(
                    timerTick = timerTick,
                    accentColor = if (pagePhase == timerTick.phase) phaseColor else pageColor
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Único: Meta Diária de Pomodoros com botões [+] e [−]
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_goal_card"),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Meta diária",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${todaySummary.completedSessions} de ${uiState.dailyGoal} pomodoros",
                        fontSize = 11.sp,
                        color = if (todaySummary.completedSessions >= uiState.dailyGoal) BreakGreen else TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.decrementDailyGoal() },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("decrement_daily_goal_button"),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = BackgroundDark,
                            contentColor = TextPrimary
                        ),
                        enabled = uiState.dailyGoal > 1
                    ) {
                        Text("−", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "${uiState.dailyGoal}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = PomodoroRed,
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .testTag("daily_goal_value_text")
                    )

                    FilledTonalButton(
                        onClick = { viewModel.incrementDailyGoal() },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("increment_daily_goal_button"),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = BackgroundDark,
                            contentColor = TextPrimary
                        )
                    ) {
                        Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Botão Principal Play / Pause
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isRunning = timerTick.state == TimerState.RUNNING
            Button(
                onClick = { viewModel.onPlayOrPauseClicked(context) },
                modifier = Modifier
                    .size(70.dp)
                    .testTag("play_pause_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = phaseColor,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pausar" else "Iniciar",
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        // Ação rápida para contabilizar tempo de pomodoro incompleto
        if (timerTick.phase == PomodoroPhase.FOCUS && timerTick.elapsedSeconds > 0) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = { viewModel.requestStopOrReset() },
                modifier = Modifier
                    .testTag("save_incomplete_session_quick_button")
                    .height(38.dp),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFF59E0B)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val mins = timerTick.elapsedSeconds / 60
                val secs = timerTick.elapsedSeconds % 60
                val timeLabel = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                Text(
                    text = "Salvar ($timeLabel)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Resumo do Dia
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Hoje",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val hours = todaySummary.totalSeconds / 3600
                    val minutes = (todaySummary.totalSeconds % 3600) / 60
                    Text(
                        text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                        .background(SurfaceCardBorder)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Concluídas",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${todaySummary.completedSessions}",
                        color = PomodoroRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Banner de aviso de agendamento automático de revisão
        uiState.reviewScheduledBanner?.let { bannerText ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.dismissReviewBanner() },
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = bannerText,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.dismissReviewBanner() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // SEÇÃO RECOLHÍVEL DE REVISÕES PERIÓDICAS (Agendadas automaticamente ao completar 2 pomodoros)
        CollapsibleReviewsSection(
            reviews = scheduledReviews,
            topicsAwaitingPomodoros = topicsAwaitingPomodoros,
            isExpanded = uiState.isReviewsSectionExpanded,
            selectedFilter = uiState.selectedReviewFilter,
            onToggleExpanded = { viewModel.toggleReviewsExpanded() },
            onSelectFilter = { viewModel.setReviewFilter(it) },
            onStartReview = { viewModel.startReviewSession(it) },
            onCompleteReview = { viewModel.completeReview(it) },
            onOpenAddReview = { viewModel.openAddReviewDialog() },
            onDeleteReview = { viewModel.deleteReviewTopic(it) },
            onStartFocusPending = { topicName, subId -> viewModel.startFocusForTopic(topicName, subId) }
        )

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Diálogo para Agendar Nova Revisão de Assunto
    if (uiState.showAddReviewDialog) {
        AddReviewDialog(
            subjects = subjects,
            selectedSubjectId = uiState.addReviewSelectedSubjectId,
            topicInput = uiState.addReviewTopicInput,
            markStudiedToday = uiState.addReviewMarkStudiedToday,
            errorMessage = uiState.addReviewError,
            onSubjectSelected = { viewModel.onAddReviewSubjectChange(it) },
            onTopicChange = { viewModel.onAddReviewTopicChange(it) },
            onMarkStudiedChange = { viewModel.onAddReviewMarkStudiedChange(it) },
            onDismiss = { viewModel.closeAddReviewDialog() },
            onConfirm = { viewModel.saveNewReview() }
        )
    }

    // DIÁLOGO DE INÍCIO / PRÉ-SESSÃO DE FOCO (Alternância entre Ciclos, Agendas e Modo Livre)
    if (uiState.showPreSessionDialog) {
        PreSessionSetupDialog(
            studyMode = uiState.studyMode,
            cycles = cycles,
            cycleItems = cycleItems,
            schedules = schedules,
            agendas = agendas,
            selectedCycleId = uiState.selectedCycleId,
            selectedAgendaId = uiState.selectedAgendaId,
            selectedScheduleDayOfWeek = uiState.selectedScheduleDayOfWeek,
            selectedScheduleId = uiState.selectedScheduleId,
            topicInput = uiState.preSessionTopicInput,
            selectedSubject = uiState.preSessionSubjectSelected,
            selectedStudyType = uiState.preSessionStudyType,
            subjects = subjects,
            topics = topics,
            errorMessage = uiState.preSessionError,
            isAutoLinked = uiState.isTopicAutoLinked,
            onModeSelected = { viewModel.setStudyMode(it) },
            onCycleSelected = { viewModel.selectStudyCycle(it) },
            onCycleSubjectSelected = { viewModel.selectSubjectFromCycle(it) },
            onAgendaSelected = { viewModel.selectAgenda(it) },
            onScheduleDaySelected = { viewModel.setScheduleDayOfWeek(it) },
            onScheduleSelected = { viewModel.selectScheduleForStudy(it) },
            onTopicChange = { viewModel.onPreSessionTopicChange(it) },
            onTopicSelectedFromList = { viewModel.selectPreSessionTopicFromList(it) },
            onSubjectSelected = { viewModel.onPreSessionSubjectSelect(it) },
            onStudyTypeSelected = { viewModel.onPreSessionStudyTypeSelect(it) },
            onDismiss = { viewModel.closePreSessionDialog() },
            onConfirm = { viewModel.confirmPreSessionAndStart() }
        )
    }

    // Diálogo de Permissão de Acessibilidade Necessária para o Bloqueador
    if (uiState.showPermissionWarningDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPermissionWarning(startAnyway = false) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Ativar Bloqueio no Android",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Para que o StudyFocus consiga fechar e bloquear aplicativos selecionados (como YouTube e Facebook) e sites durante o foco, o Android exige que você ative o Serviço de Acessibilidade.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Toque em 'Ativar nas Configurações' e ligue a chave 'StudyFocus - Bloqueio de Distrações'.",
                        color = Color(0xFFF59E0B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.openAccessibilitySettings(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("enable_accessibility_confirm_button")
                ) {
                    Text("Ativar nas Configurações", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dismissPermissionWarning(startAnyway = true) },
                    modifier = Modifier.testTag("start_without_blocking_button")
                ) {
                    Text("Iniciar sem Bloqueio", color = TextSecondary)
                }
            },
            containerColor = SurfaceCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Caixa de diálogo de Conclusão da Sessão
    if (uiState.showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCompletionDialog() },
            title = {
                Text(
                    text = "Sessão Concluída!",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Você completou ${uiState.completedDurationSecs / 60} min de estudo em '${uiState.activeTopic.ifEmpty { "Estudo Geral" }}'!",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.sessionNotes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Anotações da sessão (opcional)") },
                        placeholder = { Text("Ex: Finalizei exercícios 1 a 10") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("session_notes_input"),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveCompletedSession(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = phaseColor),
                    modifier = Modifier.testTag("save_session_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salvar Registro")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCompletionDialog() }) {
                    Text("Pular Anotação", color = TextSecondary)
                }
            }
        )
    }

    // Diálogo de Configuração de Durações
    if (uiState.showSettingsDialog) {
        TimerSettingsDialog(
            initialFocus = uiState.focusMinutes,
            initialShortBreak = uiState.shortBreakMinutes,
            initialLongBreak = uiState.longBreakMinutes,
            onDismiss = { viewModel.toggleSettingsDialog(false) },
            onConfirm = { f, s, l -> viewModel.setDurations(f, s, l) }
        )
    }

    // Diálogo para Contabilizar Pomodoro Incompleto
    if (uiState.showIncompleteSessionDialog) {
        val elapsedMinutes = uiState.incompleteSessionSecs / 60
        val elapsedSeconds = uiState.incompleteSessionSecs % 60
        val timeFormatted = if (elapsedMinutes > 0) "${elapsedMinutes}m ${elapsedSeconds}s" else "${elapsedSeconds}s"

        AlertDialog(
            onDismissRequest = { viewModel.cancelIncompleteDialog() },
            title = {
                Text(
                    text = "Contabilizar Tempo Incompleto?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Você estudou $timeFormatted neste ciclo.",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Deseja salvar e contabilizar esse tempo no histórico e estatísticas de estudo?",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = uiState.sessionNotes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Anotações da sessão (opcional)") },
                        placeholder = { Text("Ex: Interrompido para pausa") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("incomplete_notes_input"),
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveIncompleteSession(uiState.sessionNotes) },
                    colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                    modifier = Modifier.testTag("save_incomplete_session_button")
                ) {
                    Text("Salvar Tempo")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { viewModel.discardIncompleteSession() }) {
                        Text("Descartar", color = PomodoroRed)
                    }
                    TextButton(onClick = { viewModel.cancelIncompleteDialog() }) {
                        Text("Continuar", color = TextSecondary)
                    }
                }
            }
        )
    }
}

@Composable
fun PreSessionSetupDialog(
    studyMode: PomodoroStudyMode,
    cycles: List<StudyCycleEntity>,
    cycleItems: List<CycleItemWithSubject>,
    schedules: List<ScheduleWithSubject>,
    agendas: List<StudyAgendaEntity> = emptyList(),
    selectedCycleId: Long?,
    selectedAgendaId: Long? = null,
    selectedScheduleDayOfWeek: Int,
    selectedScheduleId: Long?,
    topicInput: String,
    selectedSubject: SubjectEntity?,
    selectedStudyType: StudyType,
    subjects: List<SubjectEntity>,
    topics: List<TopicEntity>,
    errorMessage: String?,
    isAutoLinked: Boolean,
    onModeSelected: (PomodoroStudyMode) -> Unit,
    onCycleSelected: (Long) -> Unit,
    onCycleSubjectSelected: (CycleItemWithSubject) -> Unit,
    onAgendaSelected: (Long) -> Unit = {},
    onScheduleDaySelected: (Int) -> Unit,
    onScheduleSelected: (ScheduleWithSubject) -> Unit,
    onTopicChange: (String) -> Unit,
    onTopicSelectedFromList: (TopicEntity) -> Unit,
    onSubjectSelected: (SubjectEntity) -> Unit,
    onStudyTypeSelected: (StudyType) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_pre_session_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar janela",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Alternador de Modo de Estudo (Segmented Tab Bar)
                Column {
                    Text(
                        text = "Modo de Planejamento",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BackgroundDark)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Aba: Ciclos de Estudo
                        val isCycle = studyMode == PomodoroStudyMode.CYCLE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCycle) PomodoroRed else Color.Transparent)
                                .clickable { onModeSelected(PomodoroStudyMode.CYCLE) }
                                .padding(vertical = 8.dp)
                                .testTag("mode_cycle_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Loop,
                                    contentDescription = null,
                                    tint = if (isCycle) Color.White else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ciclos",
                                    color = if (isCycle) Color.White else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCycle) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        // Aba: Agenda de Horários
                        val isSchedule = studyMode == PomodoroStudyMode.SCHEDULE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSchedule) PomodoroRed else Color.Transparent)
                                .clickable { onModeSelected(PomodoroStudyMode.SCHEDULE) }
                                .padding(vertical = 8.dp)
                                .testTag("mode_schedule_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (isSchedule) Color.White else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Agendas",
                                    color = if (isSchedule) Color.White else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSchedule) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        // Aba: Modo Livre
                        val isFree = studyMode == PomodoroStudyMode.FREE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isFree) PomodoroRed else Color.Transparent)
                                .clickable { onModeSelected(PomodoroStudyMode.FREE) }
                                .padding(vertical = 8.dp)
                                .testTag("mode_free_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isFree) Color.White else TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Livre",
                                    color = if (isFree) Color.White else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isFree) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 2. Conteúdo de Seleção de acordo com o Modo
                when (studyMode) {
                    PomodoroStudyMode.CYCLE -> {
                        if (cycles.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BackgroundDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Nenhum ciclo de estudos cadastrado ainda.",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Crie seus ciclos na aba 'Disciplinas' para organizar matérias em bloco, ou utilize o Modo Livre para estudar agora.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { onModeSelected(PomodoroStudyMode.FREE) },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("Usar Modo Livre", color = PomodoroRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            val activeCycleId = selectedCycleId ?: cycles.first().id
                            val activeCycle = cycles.find { it.id == activeCycleId } ?: cycles.first()
                            val items = cycleItems.filter { it.cycleId == activeCycle.id }
                            val totalMinutes = items.sumOf { it.targetMinutes }.coerceAtLeast(1)

                            // Seletor de Ciclos (se houver mais de um)
                            if (cycles.size > 1) {
                                Column {
                                    Text(
                                        text = "Ciclo Ativo:",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        cycles.forEach { cycle ->
                                            val isSelected = cycle.id == activeCycle.id
                                            Surface(
                                                modifier = Modifier.clickable { onCycleSelected(cycle.id) },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) PomodoroRed.copy(alpha = 0.2f) else BackgroundDark,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isSelected) PomodoroRed else SurfaceCardBorder
                                                )
                                            ) {
                                                Text(
                                                    text = cycle.name,
                                                    color = if (isSelected) Color.White else TextSecondary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Barra visual de proporção do ciclo
                            if (items.isNotEmpty()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${activeCycle.name} • ${items.size} matérias",
                                            fontSize = 11.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "${totalMinutes} min totais",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BackgroundDark)
                                    ) {
                                        items.forEach { item ->
                                            val color = try {
                                                Color(android.graphics.Color.parseColor(item.subjectColorHex))
                                            } catch (_: Exception) {
                                                PomodoroRed
                                            }
                                            val weight = (item.targetMinutes.toFloat() / totalMinutes.toFloat()).coerceAtLeast(0.01f)
                                            Box(
                                                modifier = Modifier
                                                    .weight(weight)
                                                    .fillMaxHeight()
                                                    .background(color)
                                            )
                                        }
                                    }
                                }

                                // Lista de Matérias do Ciclo
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Toque na matéria para focar:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    items.forEach { item ->
                                        val isCurrentSubject = selectedSubject?.id == item.subjectId
                                        val subjectColor = try {
                                            Color(android.graphics.Color.parseColor(item.subjectColorHex))
                                        } catch (_: Exception) {
                                            PomodoroRed
                                        }
                                        val percent = ((item.targetMinutes * 100) / totalMinutes)

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onCycleSubjectSelected(item) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isCurrentSubject) subjectColor.copy(alpha = 0.16f) else BackgroundDark,
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isCurrentSubject) 2.dp else 1.dp,
                                                color = if (isCurrentSubject) subjectColor else SurfaceCardBorder
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(subjectColor)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = item.subjectName,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isCurrentSubject) FontWeight.Bold else FontWeight.Medium,
                                                        color = TextPrimary
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "${item.targetMinutes}m ($percent%)",
                                                        fontSize = 12.sp,
                                                        color = if (isCurrentSubject) subjectColor else TextMuted,
                                                        fontWeight = if (isCurrentSubject) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                    if (isCurrentSubject) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selecionada",
                                                            tint = subjectColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Campo de Assunto / Detalhe Específico (preenchido com nome da matéria ou customizado)
                                Column {
                                    Text(
                                        text = "Tópico / Assunto específico:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = topicInput,
                                        onValueChange = onTopicChange,
                                        placeholder = { Text("Ex: Exercícios 1 a 10, Capítulo 3") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("pre_session_topic_input"),
                                        singleLine = true
                                    )
                                }
                            } else {
                                Text(
                                    text = "Nenhuma matéria cadastrada neste ciclo.",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    PomodoroStudyMode.SCHEDULE -> {
                        if (schedules.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BackgroundDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Nenhuma agenda cadastrada ainda.",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cadastre seus horários semanais na aba 'Disciplinas' para focar de acordo com sua grade horária.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = { onModeSelected(PomodoroStudyMode.FREE) },
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text("Usar Modo Livre", color = PomodoroRed, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // Seletor de Agendas (quando houver mais de uma cadastrada)
                            val activeAgendaId = selectedAgendaId ?: agendas.firstOrNull()?.id ?: 1L
                            if (agendas.size > 1) {
                                Column {
                                    Text(
                                        text = "Agenda Selecionada:",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        agendas.forEach { agenda ->
                                            val isSelected = agenda.id == activeAgendaId
                                            Surface(
                                                modifier = Modifier.clickable { onAgendaSelected(agenda.id) },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) PomodoroRed.copy(alpha = 0.2f) else BackgroundDark,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isSelected) PomodoroRed else SurfaceCardBorder
                                                )
                                            ) {
                                                Text(
                                                    text = agenda.name,
                                                    color = if (isSelected) Color.White else TextSecondary,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            // Seletor de Dias da Semana
                            val days = listOf("Seg" to 1, "Ter" to 2, "Qua" to 3, "Qui" to 4, "Sex" to 5, "Sáb" to 6, "Dom" to 7)
                            Column {
                                Text(
                                    text = "Dia da Semana:",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    days.forEach { (label, dayNum) ->
                                        val isSelected = selectedScheduleDayOfWeek == dayNum
                                        Surface(
                                            modifier = Modifier.clickable { onScheduleDaySelected(dayNum) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) PomodoroRed.copy(alpha = 0.2f) else BackgroundDark,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) PomodoroRed else SurfaceCardBorder
                                            )
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSelected) Color.White else TextSecondary,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Horários programados para o dia na agenda selecionada
                            val daySchedules = schedules.filter {
                                (it.agendaId == activeAgendaId || (activeAgendaId == 1L && it.agendaId == 0L)) && it.dayOfWeek == selectedScheduleDayOfWeek
                            }
                            if (daySchedules.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BackgroundDark,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Sem horários programados para este dia na grade.",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "Toque no horário agendado:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    daySchedules.forEach { item ->
                                        val isSelected = selectedScheduleId == item.id && selectedSubject?.id == item.subjectId
                                        val subjectColor = try {
                                            Color(android.graphics.Color.parseColor(item.subjectColorHex))
                                        } catch (_: Exception) {
                                            PomodoroRed
                                        }
                                        val timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d - %02d:%02d", item.startHour, item.startMinute, item.endHour, item.endMinute)

                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onScheduleSelected(item) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) subjectColor.copy(alpha = 0.16f) else BackgroundDark,
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) subjectColor else SurfaceCardBorder
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .clip(CircleShape)
                                                            .background(subjectColor)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            text = item.subjectName,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TextPrimary
                                                        )
                                                        if (item.topic.isNotBlank() || item.label.isNotBlank()) {
                                                            Text(
                                                                text = item.topic.ifBlank { item.label },
                                                                fontSize = 11.sp,
                                                                color = TextSecondary
                                                            )
                                                        }
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = timeStr,
                                                        fontSize = 11.sp,
                                                        color = if (isSelected) subjectColor else TextMuted,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    if (isSelected) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selecionado",
                                                            tint = subjectColor,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Campo de Tópico / Assunto
                                Column {
                                    Text(
                                        text = "Tópico / Assunto específico:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = topicInput,
                                        onValueChange = onTopicChange,
                                        placeholder = { Text("Ex: Tópico específico da aula") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("pre_session_topic_input"),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    PomodoroStudyMode.FREE -> {
                        // Modo Livre: Entrada de Assunto + Seleção de Matéria
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Assunto
                            Column {
                                Text(
                                    text = "Qual assunto você vai estudar?",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = topicInput,
                                    onValueChange = onTopicChange,
                                    placeholder = { Text("Ex: Funções Afins, Revolução Industrial") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pre_session_topic_input"),
                                    singleLine = true
                                )

                                // Sugestões rápidas de assuntos anteriores
                                if (topics.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        topics.take(6).forEach { topicItem ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = SurfaceCard,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                                modifier = Modifier.clickable { onTopicSelectedFromList(topicItem) }
                                            ) {
                                                Text(
                                                    text = topicItem.name,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Matéria Vinculada
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Matéria Vinculada",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    if (isAutoLinked) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = BreakGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Vínculo Automático",
                                                fontSize = 11.sp,
                                                color = BreakGreen,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    subjects.forEach { subject ->
                                        val isSelected = selectedSubject?.id == subject.id
                                        val color = try {
                                            Color(android.graphics.Color.parseColor(subject.colorHex))
                                        } catch (_: Exception) {
                                            PomodoroRed
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) color.copy(alpha = 0.2f) else BackgroundDark,
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) color else SurfaceCardBorder
                                            ),
                                            modifier = Modifier
                                                .clickable { onSubjectSelected(subject) }
                                                .testTag("subject_chip_${subject.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(color)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = subject.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) TextPrimary else TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Tipo de Estudo (Presente em todos os modos)
                Column {
                    Text(
                        text = "Tipo de Estudo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudyType.values().forEach { type ->
                            val isSelected = selectedStudyType == type
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PomodoroRed.copy(alpha = 0.18f) else BackgroundDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PomodoroRed else SurfaceCardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onStudyTypeSelected(type) }
                                    .testTag("study_type_${type.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = type.icon,
                                        contentDescription = type.label,
                                        modifier = Modifier.size(22.dp),
                                        tint = if (isSelected) PomodoroRed else TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = type.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PomodoroRed else TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Card de Prévia / Resumo da Sessão
                val subjectColor = selectedSubject?.colorHex?.let {
                    try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { PomodoroRed }
                } ?: PomodoroRed

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BackgroundDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(subjectColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = selectedSubject?.name ?: "Selecione uma matéria",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedSubject != null) TextPrimary else TextMuted
                                )
                                Text(
                                    text = topicInput.ifBlank { "Defina o assunto do foco" },
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PomodoroRed.copy(alpha = 0.15f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = selectedStudyType.icon,
                                    contentDescription = null,
                                    tint = PomodoroRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = selectedStudyType.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PomodoroRed
                                )
                            }
                        }
                    }
                }

                // Mensagem de Erro
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PomodoroRed.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PomodoroRed)
                    ) {
                        Text(
                            text = errorMessage,
                            color = PomodoroRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_pre_session_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Iniciar Foco", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun CircularTimerDisplay(
    timerTick: TimerTickState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = timerTick.progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    val minutes = timerTick.remainingSeconds / 60
    val seconds = timerTick.remainingSeconds % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val center = this.center

            drawCircle(
                color = SurfaceCard,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                color = TextPrimary,
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (timerTick.state) {
                    TimerState.IDLE -> "Pronto"
                    TimerState.RUNNING -> "Foco ativo"
                    TimerState.PAUSED -> "Pausado"
                    TimerState.COMPLETED -> "Concluído"
                },
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun TimerSettingsDialog(
    initialFocus: Int,
    initialShortBreak: Int,
    initialLongBreak: Int,
    onDismiss: () -> Unit,
    onConfirm: (focus: Int, shortBreak: Int, longBreak: Int) -> Unit
) {
    var focusText by remember { mutableStateOf(initialFocus.toString()) }
    var shortBreakText by remember { mutableStateOf(initialShortBreak.toString()) }
    var longBreakText by remember { mutableStateOf(initialLongBreak.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Durações", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tempo de Foco
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = focusText,
                        onValueChange = { focusText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Foco", fontSize = 12.sp) },
                        placeholder = { Text("25", fontSize = 12.sp) },
                        suffix = { Text("min", color = TextSecondary, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = PomodoroRed, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_focus_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(15, 25, 45, 50, 60).forEach { mins ->
                            val isSelected = focusText == mins.toString()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PomodoroRed.copy(alpha = 0.25f) else SurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) PomodoroRed else SurfaceCardBorder
                                ),
                                modifier = Modifier.clickable { focusText = mins.toString() }
                            ) {
                                Text(
                                    text = "${mins}m",
                                    color = if (isSelected) PomodoroRed else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Pausa Curta
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = shortBreakText,
                        onValueChange = { shortBreakText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Pausa Curta", fontSize = 12.sp) },
                        placeholder = { Text("5", fontSize = 12.sp) },
                        suffix = { Text("min", color = TextSecondary, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = null, tint = BreakGreen, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_short_break_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 5, 10, 15).forEach { mins ->
                            val isSelected = shortBreakText == mins.toString()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BreakGreen.copy(alpha = 0.25f) else SurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) BreakGreen else SurfaceCardBorder
                                ),
                                modifier = Modifier.clickable { shortBreakText = mins.toString() }
                            ) {
                                Text(
                                    text = "${mins}m",
                                    color = if (isSelected) BreakGreen else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Pausa Longa
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = longBreakText,
                        onValueChange = { longBreakText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Pausa Longa", fontSize = 12.sp) },
                        placeholder = { Text("15", fontSize = 12.sp) },
                        suffix = { Text("min", color = TextSecondary, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = LongBreakBlue, modifier = Modifier.size(18.dp))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setting_long_break_input"),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(10, 15, 20, 30).forEach { mins ->
                            val isSelected = longBreakText == mins.toString()
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) LongBreakBlue.copy(alpha = 0.25f) else SurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) LongBreakBlue else SurfaceCardBorder
                                ),
                                modifier = Modifier.clickable { longBreakText = mins.toString() }
                            ) {
                                Text(
                                    text = "${mins}m",
                                    color = if (isSelected) LongBreakBlue else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val focus = focusText.toIntOrNull()?.coerceIn(1, 180) ?: initialFocus
                    val shortBreak = shortBreakText.toIntOrNull()?.coerceIn(1, 60) ?: initialShortBreak
                    val longBreak = longBreakText.toIntOrNull()?.coerceIn(1, 90) ?: initialLongBreak
                    onConfirm(focus, shortBreak, longBreak)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                modifier = Modifier.testTag("save_settings_button")
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun CollapsibleReviewsSection(
    reviews: List<TopicReviewItem>,
    topicsAwaitingPomodoros: List<TopicPendingPomodoroItem> = emptyList(),
    isExpanded: Boolean,
    selectedFilter: ReviewFilter,
    onToggleExpanded: () -> Unit,
    onSelectFilter: (ReviewFilter) -> Unit,
    onStartReview: (TopicReviewItem) -> Unit,
    onCompleteReview: (TopicReviewItem) -> Unit,
    onOpenAddReview: () -> Unit,
    onDeleteReview: (TopicReviewItem) -> Unit,
    onStartFocusPending: (String, Long?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val todayReviews = remember(reviews) { reviews.filter { it.status == ReviewStatus.READY } }
    val futureReviews = remember(reviews) { reviews.filter { it.status == ReviewStatus.UPCOMING } }

    val filteredList = remember(reviews, selectedFilter) {
        when (selectedFilter) {
            ReviewFilter.TODAY -> todayReviews
            ReviewFilter.UPCOMING -> futureReviews
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("collapsible_reviews_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabeçalho / Botão Recolhível
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpanded() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("reviews_header_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (todayReviews.isNotEmpty()) Color(0xFF10B981).copy(alpha = 0.2f)
                                else BreakGreen.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = if (todayReviews.isNotEmpty()) Color(0xFF10B981) else BreakGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Revisões de Assuntos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (todayReviews.isNotEmpty()) Color(0xFF10B981) else SurfaceCardBorder)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${todayReviews.size} hoje",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val subtitleText = when {
                            todayReviews.isNotEmpty() -> "${todayReviews.size} revisão prevista para hoje • Toque para ver"
                            reviews.isNotEmpty() -> "${futureReviews.size} revisões futuras agendadas"
                            else -> "Conclua 2 pomodoros de um assunto para agendar revisões"
                        }

                        Text(
                            text = subtitleText,
                            fontSize = 12.sp,
                            color = if (todayReviews.isNotEmpty()) Color(0xFF10B981) else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onToggleExpanded() },
                        modifier = Modifier.testTag("reviews_expand_button")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Recolher revisões" else "Expandir revisões",
                            tint = TextSecondary
                        )
                    }
                }
            }

            // Conteúdo Expandido (Filtros, Lista e Ações)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .padding(bottom = 16.dp)
                ) {
                    androidx.compose.material3.HorizontalDivider(
                        color = SurfaceCardBorder,
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barra Superior: Filtros (Hoje / Futuras) & Botão "+ Novo Assunto"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReviewFilter.values().forEach { filter ->
                            val isSelected = selectedFilter == filter
                            val count = when (filter) {
                                ReviewFilter.TODAY -> todayReviews.size
                                ReviewFilter.UPCOMING -> futureReviews.size
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectFilter(filter) },
                                label = {
                                    Text(
                                        text = "${filter.label} ($count)",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (filter == ReviewFilter.TODAY) Color(0xFF10B981) else PomodoroRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = BackgroundDark,
                                    labelColor = TextSecondary
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("review_filter_${filter.name.lowercase()}")
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        OutlinedButton(
                            onClick = onOpenAddReview,
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PomodoroRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PomodoroRed),
                            modifier = Modifier.testTag("add_review_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Novo Assunto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (filteredList.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (reviews.isEmpty()) "Nenhum assunto com 2 pomodoros concluídos ainda" else "Nenhuma revisão neste filtro",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Conclua 2 pomodoros de estudo em qualquer assunto no timer. Ele entrará automaticamente no ciclo de revisões periódicas!",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onOpenAddReview,
                                    colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "Cadastrar Assunto com Estudo")
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredList.forEach { item ->
                                ReviewItemCard(
                                    item = item,
                                    onStartReview = { onStartReview(item) },
                                    onCompleteReview = { onCompleteReview(item) },
                                    onDelete = { onDeleteReview(item) }
                                )
                            }
                        }
                    }

                    // Seção de Assuntos em Preparação (1 de 2 pomodoros concluídos)
                    if (topicsAwaitingPomodoros.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Assuntos aguardando 2º pomodoro para agendar revisão:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            topicsAwaitingPomodoros.forEach { pending ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = BackgroundDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = pending.topicName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${pending.subjectName} • 1/2 pomodoros concluídos",
                                                fontSize = 11.sp,
                                                color = Color(0xFFF59E0B)
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { onStartFocusPending(pending.topicName, pending.subjectId) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, PomodoroRed),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PomodoroRed)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "Completar 2º Pomodoro", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Instruções recolhíveis posicionadas na parte inferior
                    var showExplanation by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BackgroundDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showExplanation = !showExplanation }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = PomodoroRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Como funcionam as revisões?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                                Icon(
                                    imageVector = if (showExplanation) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = showExplanation) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = "• Agendamento automático: ao concluir 2 pomodoros completos de um assunto.\n• Sem atrasos: revisões não vencem nem atrasam. A próxima data é sempre calculada a partir da última sessão realizada.\n• Ciclo de revisões periódicas: 1 dia, 7 dias, 15 dias e 30 dias.",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botão inferior para recolher a lista
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = onToggleExpanded,
                            modifier = Modifier.testTag("collapse_reviews_bottom_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandLess,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Recolher Revisões", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

private data class ReviewProgressBadge(
    val text: String,
    val isCompleted: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)

private fun getReviewBadges(item: TopicReviewItem): List<ReviewProgressBadge> {
    val count = item.reviewCount
    val isReady = item.status == ReviewStatus.READY

    return when {
        count == 0 -> {
            val currentText = if (isReady) "1d — hoje" else if (item.daysDiff > 0) "1d — em ${item.daysDiff}d" else "1d"
            listOf(
                ReviewProgressBadge(text = currentText, isCompleted = false, isToday = isReady, isFuture = !isReady),
                ReviewProgressBadge(text = "7d", isCompleted = false, isToday = false, isFuture = true),
                ReviewProgressBadge(text = "15d", isCompleted = false, isToday = false, isFuture = true)
            )
        }
        count == 1 -> {
            val currentText = if (isReady) "7d — hoje" else if (item.daysDiff > 0) "7d — em ${item.daysDiff}d" else "7d"
            listOf(
                ReviewProgressBadge(text = "✓ 1d", isCompleted = true, isToday = false, isFuture = false),
                ReviewProgressBadge(text = currentText, isCompleted = false, isToday = isReady, isFuture = !isReady),
                ReviewProgressBadge(text = "15d", isCompleted = false, isToday = false, isFuture = true)
            )
        }
        count == 2 -> {
            val currentText = if (isReady) "15d — hoje" else if (item.daysDiff > 0) "15d — em ${item.daysDiff}d" else "15d"
            listOf(
                ReviewProgressBadge(text = "✓ 7d", isCompleted = true, isToday = false, isFuture = false),
                ReviewProgressBadge(text = currentText, isCompleted = false, isToday = isReady, isFuture = !isReady),
                ReviewProgressBadge(text = "30d", isCompleted = false, isToday = false, isFuture = true)
            )
        }
        count == 3 -> {
            val currentText = if (isReady) "30d — hoje" else if (item.daysDiff > 0) "30d — em ${item.daysDiff}d" else "30d"
            listOf(
                ReviewProgressBadge(text = "✓ 15d", isCompleted = true, isToday = false, isFuture = false),
                ReviewProgressBadge(text = currentText, isCompleted = false, isToday = isReady, isFuture = !isReady),
                ReviewProgressBadge(text = "30d", isCompleted = false, isToday = false, isFuture = true)
            )
        }
        else -> {
            val currentText = if (isReady) "30d — hoje" else if (item.daysDiff > 0) "30d — em ${item.daysDiff}d" else "30d"
            listOf(
                ReviewProgressBadge(text = "✓ 30d", isCompleted = true, isToday = false, isFuture = false),
                ReviewProgressBadge(text = currentText, isCompleted = false, isToday = isReady, isFuture = !isReady),
                ReviewProgressBadge(text = "30d", isCompleted = false, isToday = false, isFuture = true)
            )
        }
    }
}

@Composable
private fun ReviewItemCard(
    item: TopicReviewItem,
    onStartReview: () -> Unit,
    onCompleteReview: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("review_item_${item.topicId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (item.status == ReviewStatus.READY) Color(0xFF10B981) else SurfaceCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Nome do Assunto e Ação de Excluir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.topicName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (item.subjectName.isNotBlank() && item.subjectName != "Sem Matéria") {
                        val subColor = try {
                            Color(android.graphics.Color.parseColor(item.subjectColorHex))
                        } catch (_: Exception) {
                            TextSecondary
                        }
                        Text(
                            text = item.subjectName,
                            fontSize = 12.sp,
                            color = subColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_review_button_${item.topicId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remover assunto das revisões",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Indicadores cronológicos claros e objetivos: [✓ 1d] [7d — hoje] [15d]
            val badges = getReviewBadges(item)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                badges.forEach { badge ->
                    val bgColor = when {
                        badge.isToday -> Color(0xFF10B981).copy(alpha = 0.22f)
                        badge.isCompleted -> SurfaceCard
                        badge.isFuture -> BackgroundDark
                        else -> PomodoroRed.copy(alpha = 0.16f)
                    }
                    val borderColor = when {
                        badge.isToday -> Color(0xFF10B981)
                        badge.isCompleted -> Color(0xFF10B981).copy(alpha = 0.6f)
                        badge.isFuture -> SurfaceCardBorder
                        else -> PomodoroRed
                    }
                    val textColor = when {
                        badge.isToday -> Color(0xFF10B981)
                        badge.isCompleted -> Color(0xFF10B981)
                        badge.isFuture -> TextMuted
                        else -> PomodoroRed
                    }
                    val isBold = badge.isToday || badge.isCompleted

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = bgColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                    ) {
                        Text(
                            text = badge.text,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botões de Ação Simples e Objetivos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartReview,
                    colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("start_review_button_${item.topicId}")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Revisar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCompleteReview,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BreakGreen),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BreakGreen),
                    modifier = Modifier.testTag("complete_review_button_${item.topicId}")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Concluir", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AddReviewDialog(
    subjects: List<SubjectEntity>,
    selectedSubjectId: Long?,
    topicInput: String,
    markStudiedToday: Boolean,
    errorMessage: String?,
    onSubjectSelected: (Long) -> Unit,
    onTopicChange: (String) -> Unit,
    onMarkStudiedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HistoryEdu,
                    contentDescription = null,
                    tint = PomodoroRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Agendar Revisão de Assunto",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cadastre um assunto. Ao marcar 'Estudado hoje', 2 pomodoros são contabilizados e a revisão entra automaticamente no ciclo. Cada revisão é calculada a partir da última sessão e nunca atrasa.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                // 1. Seleção de Disciplina
                Text(text = "Disciplina:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (subjects.isEmpty()) {
                    Text(text = "Nenhuma disciplina cadastrada.", fontSize = 12.sp, color = TextMuted)
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        subjects.forEach { subject ->
                            val isSelected = selectedSubjectId == subject.id
                            val color = try {
                                Color(android.graphics.Color.parseColor(subject.colorHex))
                            } catch (_: Exception) {
                                PomodoroRed
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { onSubjectSelected(subject.id) },
                                label = { Text(subject.name, fontSize = 12.sp) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = color.copy(alpha = 0.25f),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // 2. Campo do Assunto
                OutlinedTextField(
                    value = topicInput,
                    onValueChange = onTopicChange,
                    label = { Text("Nome do Assunto") },
                    placeholder = { Text("Ex: Funções Trigonométricas, Sintaxe...") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_review_topic_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Text(text = errorMessage, color = PomodoroRed, fontSize = 12.sp)
                }

                // 3. Opção de Estudo Inicial
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMarkStudiedChange(!markStudiedToday) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = markStudiedToday,
                        onCheckedChange = onMarkStudiedChange,
                        colors = CheckboxDefaults.colors(checkedColor = PomodoroRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Assunto estudado hoje (2 pomodoros completos)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Ativa o ciclo: 1ª revisão calculada para 24h após o estudo",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                modifier = Modifier.testTag("confirm_add_review_button")
            ) {
                Text("Agendar Revisão")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

private fun formatReviewDate(epochMs: Long): String {
    if (epochMs <= 0) return "Nenhuma"
    return try {
        val date = java.time.Instant.ofEpochMilli(epochMs)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        val today = java.time.LocalDate.now()
        val diff = java.time.temporal.ChronoUnit.DAYS.between(today, date)
        when {
            diff == 0L -> "Hoje"
            diff == 1L -> "Amanhã"
            diff == -1L -> "Ontem"
            diff < -1L -> "Há ${-diff} dias"
            diff in 2L..6L -> "Em $diff dias"
            else -> String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", date.dayOfMonth, date.monthValue, date.year)
        }
    } catch (_: Exception) {
        "Data indefinida"
    }
}

private fun formatTimestamp(epochMs: Long): String {
    if (epochMs <= 0) return "Nenhuma"
    return try {
        val date = java.time.Instant.ofEpochMilli(epochMs)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
        String.format(java.util.Locale.getDefault(), "%02d/%02d", date.dayOfMonth, date.monthValue)
    } catch (_: Exception) {
        ""
    }
}


