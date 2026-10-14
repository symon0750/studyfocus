package com.example.ui.pomodoro

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.DailySummaryDto
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.data.repository.StudyRepository
import com.example.domain.model.PomodoroPhase
import com.example.domain.model.StudyType
import com.example.domain.model.TimerState
import com.example.domain.model.TimerTickState
import com.example.service.FocusBlockManager
import com.example.service.FocusMonitoringService
import com.example.service.PomodoroTimerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.data.local.dao.CycleItemWithSubject
import com.example.data.local.dao.ScheduleWithSubject
import com.example.data.local.entity.StudyAgendaEntity
import com.example.data.local.entity.StudyCycleEntity
import com.example.data.local.entity.StudyScheduleEntity
import com.example.data.repository.TopicReviewItem
import com.example.data.repository.TopicPendingPomodoroItem
import com.example.data.repository.ReviewStatus

enum class PomodoroStudyMode {
    CYCLE,
    SCHEDULE,
    FREE
}

enum class ReviewFilter(val label: String) {
    TODAY("Hoje"),
    UPCOMING("Futuras")
}

data class PomodoroUiState(
    val activeTopic: String = "",
    val activeSubject: SubjectEntity? = null,
    val activeStudyType: StudyType = StudyType.READING,
    val isSessionConfigured: Boolean = false,
    val studyMode: PomodoroStudyMode = PomodoroStudyMode.CYCLE,
    val selectedCycleId: Long? = null,
    val selectedAgendaId: Long? = null,
    val selectedScheduleDayOfWeek: Int = java.time.LocalDate.now().dayOfWeek.value, // 1 to 7
    val selectedScheduleId: Long? = null,
    val showPreSessionDialog: Boolean = false,
    val preSessionTopicInput: String = "",
    val preSessionSubjectSelected: SubjectEntity? = null,
    val preSessionStudyType: StudyType = StudyType.READING,
    val preSessionError: String? = null,
    val isTopicAutoLinked: Boolean = false,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val showCompletionDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showIncompleteSessionDialog: Boolean = false,
    val incompleteSessionSecs: Int = 0,
    val sessionNotes: String = "",
    val completedPhase: PomodoroPhase = PomodoroPhase.FOCUS,
    val completedDurationSecs: Int = 0,
    val showPermissionWarningDialog: Boolean = false,
    val hasDismissedPermissionWarning: Boolean = false,
    val isReviewsSectionExpanded: Boolean = false,
    val selectedReviewFilter: ReviewFilter = ReviewFilter.TODAY,
    val showAddReviewDialog: Boolean = false,
    val addReviewSelectedSubjectId: Long? = null,
    val addReviewTopicInput: String = "",
    val addReviewMarkStudiedToday: Boolean = true,
    val addReviewError: String? = null,
    val reviewScheduledBanner: String? = null,
    val dailyGoal: Int = 4
)

class PomodoroViewModel(
    private val repository: StudyRepository,
    val timerManager: PomodoroTimerManager = PomodoroTimerManager(),
    private val application: Application? = null
) : ViewModel() {

    val timerState: StateFlow<TimerTickState> = timerManager.timerState

    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val topics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val studyCycles: StateFlow<List<StudyCycleEntity>> = repository.allCycles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cycleItems: StateFlow<List<CycleItemWithSubject>> = repository.allCycleItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val schedules: StateFlow<List<ScheduleWithSubject>> = repository.allSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val agendas: StateFlow<List<StudyAgendaEntity>> = repository.allAgendas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todaySummary: StateFlow<DailySummaryDto> = repository.getTodaySummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailySummaryDto(0, 0, 0)
        )

    val scheduledReviews: StateFlow<List<TopicReviewItem>> = repository.scheduledReviews
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val topicsAwaitingPomodoros: StateFlow<List<TopicPendingPomodoroItem>> = repository.topicsAwaitingPomodoros
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private val prefs = application?.getSharedPreferences("pomodoro_prefs", Context.MODE_PRIVATE)

    fun selectPhase(phase: PomodoroPhase) {
        val durationMinutes = when (phase) {
            PomodoroPhase.FOCUS -> _uiState.value.focusMinutes
            PomodoroPhase.SHORT_BREAK -> _uiState.value.shortBreakMinutes
            PomodoroPhase.LONG_BREAK -> _uiState.value.longBreakMinutes
        }
        timerManager.setPhase(phase, durationMinutes)
    }

    fun incrementDailyGoal() {
        val newGoal = (_uiState.value.dailyGoal + 1).coerceAtMost(99)
        _uiState.update { it.copy(dailyGoal = newGoal) }
        prefs?.edit()?.putInt("daily_pomodoro_goal", newGoal)?.apply()
    }

    fun decrementDailyGoal() {
        val newGoal = (_uiState.value.dailyGoal - 1).coerceAtLeast(1)
        _uiState.update { it.copy(dailyGoal = newGoal) }
        prefs?.edit()?.putInt("daily_pomodoro_goal", newGoal)?.apply()
    }

    fun toggleReviewsExpanded(expanded: Boolean? = null) {
        _uiState.update { current ->
            current.copy(isReviewsSectionExpanded = expanded ?: !current.isReviewsSectionExpanded)
        }
    }

    fun setReviewFilter(filter: ReviewFilter) {
        _uiState.update { it.copy(selectedReviewFilter = filter) }
    }

    fun dismissReviewBanner() {
        _uiState.update { it.copy(reviewScheduledBanner = null) }
    }

    fun startFocusForTopic(topicName: String, subjectId: Long?) {
        val matchedSubject = subjects.value.find { it.id == subjectId }
        _uiState.update {
            it.copy(
                showPreSessionDialog = true,
                preSessionTopicInput = topicName,
                preSessionSubjectSelected = matchedSubject,
                preSessionStudyType = StudyType.EXERCISES,
                preSessionError = null,
                studyMode = PomodoroStudyMode.FREE
            )
        }
    }

    fun startReviewSession(item: TopicReviewItem) {
        val matchedSubject = subjects.value.find { it.id == item.subjectId }
            ?: SubjectEntity(id = item.subjectId ?: 0L, name = item.subjectName, colorHex = item.subjectColorHex)
        _uiState.update {
            it.copy(
                showPreSessionDialog = true,
                preSessionTopicInput = item.topicName,
                preSessionSubjectSelected = matchedSubject,
                preSessionStudyType = StudyType.REVIEW,
                preSessionError = null,
                studyMode = PomodoroStudyMode.FREE
            )
        }
    }

    fun completeReview(item: TopicReviewItem) {
        viewModelScope.launch {
            repository.completeReview(
                subjectId = item.subjectId,
                topicName = item.topicName
            )
        }
    }

    fun openAddReviewDialog(defaultSubjectId: Long? = null) {
        val initialSubject = defaultSubjectId ?: subjects.value.firstOrNull()?.id
        _uiState.update {
            it.copy(
                showAddReviewDialog = true,
                addReviewSelectedSubjectId = initialSubject,
                addReviewTopicInput = "",
                addReviewMarkStudiedToday = true,
                addReviewError = null
            )
        }
    }

    fun closeAddReviewDialog() {
        _uiState.update { it.copy(showAddReviewDialog = false, addReviewError = null) }
    }

    fun onAddReviewSubjectChange(subjectId: Long) {
        _uiState.update { it.copy(addReviewSelectedSubjectId = subjectId) }
    }

    fun onAddReviewTopicChange(topic: String) {
        _uiState.update { it.copy(addReviewTopicInput = topic, addReviewError = null) }
    }

    fun onAddReviewMarkStudiedChange(mark: Boolean) {
        _uiState.update { it.copy(addReviewMarkStudiedToday = mark) }
    }

    fun saveNewReview() {
        val topic = _uiState.value.addReviewTopicInput.trim()
        val subjectId = _uiState.value.addReviewSelectedSubjectId
        if (topic.isEmpty()) {
            _uiState.update { it.copy(addReviewError = "Digite o nome do assunto a ser revisado") }
            return
        }
        if (subjectId == null) {
            _uiState.update { it.copy(addReviewError = "Selecione uma disciplina para o assunto") }
            return
        }
        val markStudied = _uiState.value.addReviewMarkStudiedToday
        viewModelScope.launch {
            repository.createTopicReview(subjectId, topic, hasInitialStudy = markStudied)
            closeAddReviewDialog()
        }
    }

    fun deleteReviewTopic(item: TopicReviewItem) {
        viewModelScope.launch {
            repository.deleteTopicById(item.topicId)
        }
    }

    fun setStudyMode(mode: PomodoroStudyMode) {
        _uiState.update { it.copy(studyMode = mode, preSessionError = null) }
    }

    fun selectStudyCycle(cycleId: Long) {
        _uiState.update { it.copy(selectedCycleId = cycleId) }
    }

    fun setScheduleDayOfWeek(day: Int) {
        _uiState.update { it.copy(selectedScheduleDayOfWeek = day) }
    }

    fun selectAgenda(agendaId: Long) {
        _uiState.update { it.copy(selectedAgendaId = agendaId) }
    }

    fun selectSubjectFromCycle(item: CycleItemWithSubject) {
        val subject = subjects.value.find { it.id == item.subjectId }
            ?: SubjectEntity(id = item.subjectId, name = item.subjectName, colorHex = item.subjectColorHex)
        _uiState.update {
            it.copy(
                preSessionSubjectSelected = subject,
                // O campo de tópico permanece independente (não é preenchido com o nome da matéria)
                preSessionError = null
            )
        }
    }

    fun selectScheduleForStudy(schedule: ScheduleWithSubject) {
        val subject = subjects.value.find { it.id == schedule.subjectId }
            ?: SubjectEntity(id = schedule.subjectId, name = schedule.subjectName, colorHex = schedule.subjectColorHex)
        _uiState.update { currentState ->
            currentState.copy(
                selectedScheduleId = schedule.id,
                preSessionSubjectSelected = subject,
                // O campo de tópico/assunto permanece independente
                preSessionError = null
            )
        }
    }

    init {
        val savedDailyGoal = prefs?.getInt("daily_pomodoro_goal", 4) ?: 4
        _uiState.update { it.copy(dailyGoal = savedDailyGoal) }

        // Observa término do timer para disparar conclusão, vibração e controle de bloqueio
        viewModelScope.launch {
            timerState.collectLatest { tick ->
                // Atualiza o motor central de bloqueio em tempo real
                FocusBlockManager.updatePomodoroState(
                    state = tick.state,
                    phase = tick.phase,
                    remainingSeconds = tick.remainingSeconds,
                    subjectName = _uiState.value.activeSubject?.name ?: "",
                    topicName = _uiState.value.activeTopic
                )

                // Gerencia o serviço em primeiro plano
                application?.let { app ->
                    val blockSettings = FocusBlockManager.runtimeState.value
                    if (tick.state == TimerState.RUNNING && (tick.phase == PomodoroPhase.FOCUS || blockSettings.blockDuringBreaks)) {
                        FocusMonitoringService.start(app)
                    } else if (tick.state != TimerState.RUNNING) {
                        FocusMonitoringService.stop(app)
                    }
                }

                if (tick.state == TimerState.COMPLETED && !_uiState.value.showCompletionDialog) {
                    _uiState.update {
                        it.copy(
                            showCompletionDialog = true,
                            completedPhase = tick.phase,
                            completedDurationSecs = tick.totalSeconds
                        )
                    }
                }
            }
        }
    }

    fun openPreSessionDialog() {
        val current = _uiState.value
        val initialCycleId = current.selectedCycleId ?: studyCycles.value.firstOrNull()?.id
        _uiState.update {
            it.copy(
                showPreSessionDialog = true,
                selectedCycleId = initialCycleId,
                preSessionTopicInput = current.activeTopic,
                preSessionSubjectSelected = current.activeSubject,
                preSessionStudyType = current.activeStudyType,
                preSessionError = null,
                isTopicAutoLinked = false
            )
        }
    }

    fun closePreSessionDialog() {
        _uiState.update { it.copy(showPreSessionDialog = false, preSessionError = null) }
    }

    fun onPreSessionTopicChange(topicText: String) {
        _uiState.update {
            it.copy(
                preSessionTopicInput = topicText,
                preSessionError = null
            )
        }
        // Vínculo automático: verifica se o assunto já existe no histórico
        viewModelScope.launch {
            val trimmed = topicText.trim()
            if (trimmed.isNotEmpty()) {
                val matchedSubject = repository.getSubjectForTopic(trimmed)
                if (matchedSubject != null) {
                    _uiState.update {
                        it.copy(
                            preSessionSubjectSelected = matchedSubject,
                            isTopicAutoLinked = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(isTopicAutoLinked = false) }
                }
            } else {
                _uiState.update { it.copy(isTopicAutoLinked = false) }
            }
        }
    }

    fun selectPreSessionTopicFromList(topic: TopicEntity) {
        _uiState.update {
            it.copy(
                preSessionTopicInput = topic.name,
                preSessionError = null
            )
        }
        viewModelScope.launch {
            val matchedSubject = repository.getSubjectById(topic.subjectId)
            if (matchedSubject != null) {
                _uiState.update {
                    it.copy(
                        preSessionSubjectSelected = matchedSubject,
                        isTopicAutoLinked = true
                    )
                }
            }
        }
    }

    fun onPreSessionSubjectSelect(subject: SubjectEntity) {
        _uiState.update {
            it.copy(
                preSessionSubjectSelected = subject,
                isTopicAutoLinked = false,
                preSessionError = null
            )
        }
    }

    fun onPreSessionStudyTypeSelect(type: StudyType) {
        _uiState.update { it.copy(preSessionStudyType = type) }
    }

    fun confirmPreSessionAndStart() {
        val topic = _uiState.value.preSessionTopicInput.trim()
        val subject = _uiState.value.preSessionSubjectSelected

        if (topic.isEmpty()) {
            _uiState.update { it.copy(preSessionError = "Digite o assunto da sessão de estudo.") }
            return
        }

        if (subject == null) {
            _uiState.update { it.copy(preSessionError = "Selecione a matéria vinculada ao assunto.") }
            return
        }

        val studyType = _uiState.value.preSessionStudyType

        viewModelScope.launch {
            // Salva mapeamento automático assunto -> matéria
            repository.associateTopicWithSubject(topic, subject.id)

            _uiState.update {
                it.copy(
                    activeTopic = topic,
                    activeSubject = subject,
                    activeStudyType = studyType,
                    isSessionConfigured = true,
                    showPreSessionDialog = false,
                    preSessionError = null
                )
            }

            // Inicia o timer
            val targetMinutes = when (timerState.value.phase) {
                PomodoroPhase.FOCUS -> _uiState.value.focusMinutes
                PomodoroPhase.SHORT_BREAK -> _uiState.value.shortBreakMinutes
                PomodoroPhase.LONG_BREAK -> _uiState.value.longBreakMinutes
            }
            timerManager.startOrResume(targetMinutes = targetMinutes)
        }
    }

    fun onPlayOrPauseClicked(context: Context? = null) {
        val tick = timerState.value
        when (tick.state) {
            TimerState.RUNNING -> {
                timerManager.pause()
            }
            TimerState.PAUSED -> {
                timerManager.resume()
            }
            TimerState.IDLE, TimerState.COMPLETED -> {
                if (tick.phase == PomodoroPhase.FOCUS) {
                    // Se o bloqueio está ativado mas permissões do Android estão pendentes
                    if (context != null && !_uiState.value.hasDismissedPermissionWarning) {
                        val runtimeState = FocusBlockManager.runtimeState.value
                        val hasAccessibility = FocusBlockManager.isAccessibilityServiceEnabled(
                            context,
                            com.example.service.FocusBlockAccessibilityService::class.java
                        )
                        val hasUsage = FocusBlockManager.hasUsageStatsPermission(context)
                        if (runtimeState.isBlockingEnabled && !hasAccessibility && !hasUsage) {
                            _uiState.update { it.copy(showPermissionWarningDialog = true) }
                            return
                        }
                    }

                    // A contagem do pomodoro abre a janela para escolha entre ciclos, agendas ou modo livre
                    openPreSessionDialog()
                } else {
                    val breakMinutes = if (tick.phase == PomodoroPhase.SHORT_BREAK) {
                        _uiState.value.shortBreakMinutes
                    } else {
                        _uiState.value.longBreakMinutes
                    }
                    timerManager.startOrResume(targetMinutes = breakMinutes)
                }
            }
        }
    }

    fun dismissPermissionWarning(startAnyway: Boolean = false) {
        _uiState.update {
            it.copy(
                showPermissionWarningDialog = false,
                hasDismissedPermissionWarning = true
            )
        }
        if (startAnyway) {
            openPreSessionDialog()
        }
    }

    fun openAccessibilitySettings(context: Context) {
        FocusBlockManager.openAccessibilitySettings(context)
        _uiState.update { it.copy(showPermissionWarningDialog = false) }
    }

    fun updateNotes(notes: String) {
        _uiState.update { it.copy(sessionNotes = notes) }
    }

    fun setDurations(focus: Int, shortBreak: Int, longBreak: Int) {
        _uiState.update {
            it.copy(
                focusMinutes = focus,
                shortBreakMinutes = shortBreak,
                longBreakMinutes = longBreak,
                showSettingsDialog = false
            )
        }
        if (timerState.value.state == TimerState.IDLE) {
            timerManager.stopAndReset()
        }
    }

    fun toggleSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun requestStopOrReset() {
        val tick = timerState.value
        if (tick.elapsedSeconds > 0 && tick.phase == PomodoroPhase.FOCUS) {
            timerManager.pause()
            _uiState.update {
                it.copy(
                    showIncompleteSessionDialog = true,
                    incompleteSessionSecs = tick.elapsedSeconds
                )
            }
        } else {
            timerManager.stopAndReset()
        }
    }

    fun saveIncompleteSession(notes: String = "") {
        val tick = timerState.value
        val elapsed = if (_uiState.value.incompleteSessionSecs > 0) _uiState.value.incompleteSessionSecs else tick.elapsedSeconds
        if (elapsed > 0 && tick.phase == PomodoroPhase.FOCUS) {
            val startTime = if (tick.sessionStartEpochMs > 0) tick.sessionStartEpochMs else System.currentTimeMillis() - (elapsed * 1000L)
            viewModelScope.launch {
                repository.recordSession(
                    subjectId = _uiState.value.activeSubject?.id,
                    topic = _uiState.value.activeTopic.ifEmpty { "Estudo Parcial" },
                    studyType = _uiState.value.activeStudyType.id,
                    phaseType = tick.phase.name,
                    targetDurationSeconds = tick.totalSeconds,
                    actualDurationSeconds = elapsed,
                    startTimeEpochMs = startTime,
                    endTimeEpochMs = System.currentTimeMillis(),
                    completed = false,
                    notes = if (notes.isBlank()) "Ciclo finalizado antecipadamente" else notes
                )
            }
        }
        _uiState.update { it.copy(showIncompleteSessionDialog = false, incompleteSessionSecs = 0, sessionNotes = "") }
        timerManager.stopAndReset()
    }

    fun discardIncompleteSession() {
        _uiState.update { it.copy(showIncompleteSessionDialog = false, incompleteSessionSecs = 0, sessionNotes = "") }
        timerManager.stopAndReset()
    }

    fun cancelIncompleteDialog() {
        _uiState.update { it.copy(showIncompleteSessionDialog = false) }
    }

    fun stopAndReset(savePartial: Boolean = false) {
        requestStopOrReset()
    }

    fun skipPhase() {
        val tick = timerState.value
        if (tick.elapsedSeconds > 0 && tick.phase == PomodoroPhase.FOCUS) {
            timerManager.pause()
            _uiState.update {
                it.copy(
                    showIncompleteSessionDialog = true,
                    incompleteSessionSecs = tick.elapsedSeconds
                )
            }
        } else {
            timerManager.skipToNextPhase()
        }
    }

    fun saveCompletedSession(context: Context? = null) {
        val tick = timerState.value
        val startTime = if (tick.sessionStartEpochMs > 0) tick.sessionStartEpochMs else System.currentTimeMillis() - (_uiState.value.completedDurationSecs * 1000L)

        val activeTopicName = _uiState.value.activeTopic
        val isStudyFocus = _uiState.value.completedPhase == PomodoroPhase.FOCUS &&
                           _uiState.value.activeStudyType != StudyType.REVIEW

        viewModelScope.launch {
            repository.recordSession(
                subjectId = _uiState.value.activeSubject?.id,
                topic = _uiState.value.activeTopic,
                studyType = _uiState.value.activeStudyType.id,
                phaseType = _uiState.value.completedPhase.name,
                targetDurationSeconds = _uiState.value.completedDurationSecs,
                actualDurationSeconds = _uiState.value.completedDurationSecs,
                startTimeEpochMs = startTime,
                endTimeEpochMs = System.currentTimeMillis(),
                completed = true,
                notes = _uiState.value.sessionNotes
            )

            // Feedback se o assunto concluiu 2 pomodoros
            val bannerNotice = if (isStudyFocus && activeTopicName.isNotBlank()) {
                "Sessão salva com sucesso!"
            } else null

            _uiState.update {
                it.copy(
                    showCompletionDialog = false,
                    sessionNotes = "",
                    reviewScheduledBanner = bannerNotice
                )
            }
            timerManager.skipToNextPhase()
        }

        triggerVibration(context)
    }

    fun dismissCompletionDialog() {
        _uiState.update { it.copy(showCompletionDialog = false, sessionNotes = "") }
        timerManager.skipToNextPhase()
    }

    private fun triggerVibration(context: Context?) {
        if (context == null) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Exception) {}
    }
}

class PomodoroViewModelFactory(
    private val repository: StudyRepository,
    private val application: Application? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PomodoroViewModel(repository, application = application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
