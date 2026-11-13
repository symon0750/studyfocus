package com.example.ui.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.CycleItemWithSubject
import com.example.data.local.dao.ScheduleWithSubject
import com.example.data.local.entity.StudyAgendaEntity
import com.example.data.local.entity.StudyCycleEntity
import com.example.data.local.entity.StudyScheduleEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubjectsUiState(
    val selectedTab: Int = 0, // 0: Disciplinas & Assuntos, 1: Ciclos de Estudo, 2: Agendas de Horários
    // Disciplinas
    val showAddEditSubjectDialog: Boolean = false,
    val editingSubject: SubjectEntity? = null,
    val subjectNameInput: String = "",
    val selectedColorHex: String = "#3B82F6",
    val subjectError: String? = null,
    val expandedSubjectIds: Set<Long> = emptySet(),
    // Assuntos (Tópicos)
    val editingTopic: TopicEntity? = null,
    val showEditTopicDialog: Boolean = false,
    val topicEditNameInput: String = "",
    val topicError: String? = null,
    // Ciclos de Estudo
    val showAddEditCycleDialog: Boolean = false,
    val editingCycle: StudyCycleEntity? = null,
    val cycleNameInput: String = "",
    val cycleDescriptionInput: String = "",
    val cycleItemsDraft: List<Pair<Long, Int>> = emptyList(), // subjectId to targetMinutes
    val cycleError: String? = null,
    // Agendas de Horários
    val selectedAgendaId: Long? = null,
    val showAddEditAgendaDialog: Boolean = false,
    val editingAgenda: StudyAgendaEntity? = null,
    val agendaNameInput: String = "",
    val agendaDescriptionInput: String = "",
    val agendaError: String? = null,
    // Horários (Schedules dentro da Agenda)
    val showAddEditScheduleDialog: Boolean = false,
    val editingSchedule: ScheduleWithSubject? = null,
    val scheduleDayOfWeek: Int = 1,
    val scheduleStartHour: Int = 8,
    val scheduleStartMinute: Int = 0,
    val scheduleEndHour: Int = 10,
    val scheduleEndMinute: Int = 0,
    val scheduleSubjectId: Long? = null,
    val scheduleTopicInput: String = "",
    val scheduleLabelInput: String = "",
    val scheduleError: String? = null
)

class SubjectsViewModel(
    private val repository: StudyRepository
) : ViewModel() {

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

    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            agendas.collect { list ->
                if (_uiState.value.selectedAgendaId == null && list.isNotEmpty()) {
                    _uiState.update { it.copy(selectedAgendaId = list.first().id) }
                }
            }
        }
    }

    val availableColors = listOf(
        "#3B82F6", // Blue
        "#10B981", // Emerald
        "#F59E0B", // Amber
        "#EF4444", // Red
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#06B6D4", // Cyan
        "#14B8A6"  // Teal
    )

    fun selectTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // ==========================================
    // Disciplinas (CRUD) & Expansão Recolhível
    // ==========================================

    fun toggleSubjectExpansion(subjectId: Long) {
        _uiState.update { current ->
            val set = current.expandedSubjectIds.toMutableSet()
            if (set.contains(subjectId)) {
                set.remove(subjectId)
            } else {
                set.add(subjectId)
            }
            current.copy(expandedSubjectIds = set)
        }
    }

    fun openAddSubjectDialog() {
        _uiState.update {
            it.copy(
                showAddEditSubjectDialog = true,
                editingSubject = null,
                subjectNameInput = "",
                selectedColorHex = availableColors.first(),
                subjectError = null
            )
        }
    }

    fun openEditSubjectDialog(subject: SubjectEntity) {
        _uiState.update {
            it.copy(
                showAddEditSubjectDialog = true,
                editingSubject = subject,
                subjectNameInput = subject.name,
                selectedColorHex = subject.colorHex,
                subjectError = null
            )
        }
    }

    fun closeSubjectDialog() {
        _uiState.update { it.copy(showAddEditSubjectDialog = false, subjectError = null) }
    }

    fun onSubjectNameChange(name: String) {
        _uiState.update { it.copy(subjectNameInput = name, subjectError = null) }
    }

    fun onSubjectColorSelect(colorHex: String) {
        _uiState.update { it.copy(selectedColorHex = colorHex) }
    }

    fun saveSubject() {
        val name = _uiState.value.subjectNameInput.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(subjectError = "O nome da disciplina não pode ficar vazio") }
            return
        }

        val color = _uiState.value.selectedColorHex
        val currentEdit = _uiState.value.editingSubject

        viewModelScope.launch {
            if (currentEdit != null) {
                repository.updateSubject(currentEdit.copy(name = name, colorHex = color))
            } else {
                val newId = repository.createSubject(name = name, colorHex = color)
                // Já abre a disciplina expandida para poder adicionar tópicos
                _uiState.update { it.copy(expandedSubjectIds = it.expandedSubjectIds + newId) }
            }
            closeSubjectDialog()
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    // ==========================================
    // Assuntos (Tópicos) de Cada Disciplina (CRUD)
    // ==========================================

    fun addTopicToSubject(subjectId: Long, topicName: String) {
        val trimmed = topicName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.createTopic(trimmed, subjectId)
        }
    }

    fun openEditTopicDialog(topic: TopicEntity) {
        _uiState.update {
            it.copy(
                showEditTopicDialog = true,
                editingTopic = topic,
                topicEditNameInput = topic.name,
                topicError = null
            )
        }
    }

    fun closeEditTopicDialog() {
        _uiState.update { it.copy(showEditTopicDialog = false, editingTopic = null, topicError = null) }
    }

    fun onTopicEditNameChange(name: String) {
        _uiState.update { it.copy(topicEditNameInput = name, topicError = null) }
    }

    fun saveTopicEdit() {
        val name = _uiState.value.topicEditNameInput.trim()
        val topic = _uiState.value.editingTopic ?: return
        if (name.isEmpty()) {
            _uiState.update { it.copy(topicError = "O nome do assunto não pode ficar vazio") }
            return
        }
        viewModelScope.launch {
            repository.updateTopic(topic.copy(name = name))
            closeEditTopicDialog()
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
        }
    }

    // ==========================================
    // Ciclos de Estudo (CRUD)
    // ==========================================

    fun openAddCycleDialog() {
        val available = subjects.value
        val initialItems = if (available.isNotEmpty()) {
            available.take(3).map { it.id to 60 }
        } else {
            emptyList()
        }
        _uiState.update {
            it.copy(
                showAddEditCycleDialog = true,
                editingCycle = null,
                cycleNameInput = "",
                cycleDescriptionInput = "",
                cycleItemsDraft = initialItems,
                cycleError = null
            )
        }
    }

    fun openEditCycleDialog(cycle: StudyCycleEntity) {
        val currentItems = cycleItems.value.filter { it.cycleId == cycle.id }
        _uiState.update {
            it.copy(
                showAddEditCycleDialog = true,
                editingCycle = cycle,
                cycleNameInput = cycle.name,
                cycleDescriptionInput = cycle.description,
                cycleItemsDraft = currentItems.map { item -> item.subjectId to item.targetMinutes },
                cycleError = null
            )
        }
    }

    fun closeCycleDialog() {
        _uiState.update { it.copy(showAddEditCycleDialog = false, cycleError = null) }
    }

    fun onCycleNameChange(name: String) {
        _uiState.update { it.copy(cycleNameInput = name, cycleError = null) }
    }

    fun onCycleDescriptionChange(desc: String) {
        _uiState.update { it.copy(cycleDescriptionInput = desc) }
    }

    fun addSubjectToCycleDraft(subjectId: Long, minutes: Int = 60) {
        _uiState.update { current ->
            if (current.cycleItemsDraft.any { it.first == subjectId }) {
                current
            } else {
                current.copy(cycleItemsDraft = current.cycleItemsDraft + (subjectId to minutes))
            }
        }
    }

    fun removeSubjectFromCycleDraft(subjectId: Long) {
        _uiState.update { current ->
            current.copy(cycleItemsDraft = current.cycleItemsDraft.filter { it.first != subjectId })
        }
    }

    fun updateCycleDraftItemDuration(subjectId: Long, minutes: Int) {
        _uiState.update { current ->
            val updated = current.cycleItemsDraft.map {
                if (it.first == subjectId) it.first to minutes.coerceIn(1, 720) else it
            }
            current.copy(cycleItemsDraft = updated)
        }
    }

    fun saveCycle() {
        val name = _uiState.value.cycleNameInput.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(cycleError = "Digite um nome para o ciclo") }
            return
        }
        val items = _uiState.value.cycleItemsDraft
        if (items.isEmpty()) {
            _uiState.update { it.copy(cycleError = "Adicione ao menos uma matéria ao ciclo") }
            return
        }

        val desc = _uiState.value.cycleDescriptionInput.trim()
        val currentEdit = _uiState.value.editingCycle

        viewModelScope.launch {
            if (currentEdit != null) {
                repository.updateStudyCycle(currentEdit.copy(name = name, description = desc), items)
            } else {
                repository.createStudyCycle(name, desc, items)
            }
            closeCycleDialog()
        }
    }

    fun deleteCycle(cycle: StudyCycleEntity) {
        viewModelScope.launch {
            repository.deleteStudyCycle(cycle)
        }
    }

    // ==========================================
    // Múltiplas Agendas de Horários (CRUD)
    // ==========================================

    fun selectAgenda(agendaId: Long) {
        _uiState.update { it.copy(selectedAgendaId = agendaId) }
    }

    fun openAddAgendaDialog() {
        _uiState.update {
            it.copy(
                showAddEditAgendaDialog = true,
                editingAgenda = null,
                agendaNameInput = "",
                agendaDescriptionInput = "",
                agendaError = null
            )
        }
    }

    fun openEditAgendaDialog(agenda: StudyAgendaEntity) {
        _uiState.update {
            it.copy(
                showAddEditAgendaDialog = true,
                editingAgenda = agenda,
                agendaNameInput = agenda.name,
                agendaDescriptionInput = agenda.description,
                agendaError = null
            )
        }
    }

    fun closeAgendaDialog() {
        _uiState.update { it.copy(showAddEditAgendaDialog = false, agendaError = null) }
    }

    fun onAgendaNameChange(name: String) {
        _uiState.update { it.copy(agendaNameInput = name, agendaError = null) }
    }

    fun onAgendaDescriptionChange(desc: String) {
        _uiState.update { it.copy(agendaDescriptionInput = desc) }
    }

    fun saveAgenda() {
        val name = _uiState.value.agendaNameInput.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(agendaError = "Informe o nome da agenda") }
            return
        }

        val editing = _uiState.value.editingAgenda
        val entity = StudyAgendaEntity(
            id = editing?.id ?: 0L,
            name = name,
            description = _uiState.value.agendaDescriptionInput.trim(),
            isActive = editing?.isActive ?: true
        )

        viewModelScope.launch {
            if (editing != null) {
                repository.updateAgenda(entity)
            } else {
                val newId = repository.createAgenda(entity)
                _uiState.update { it.copy(selectedAgendaId = newId) }
            }
            closeAgendaDialog()
        }
    }

    fun deleteAgenda(agendaId: Long) {
        viewModelScope.launch {
            repository.deleteAgenda(agendaId)
            val remaining = agendas.value.filter { it.id != agendaId }
            _uiState.update { it.copy(selectedAgendaId = remaining.firstOrNull()?.id) }
        }
    }

    // ==========================================
    // Horários da Agenda Selecionada (CRUD)
    // ==========================================

    fun openAddScheduleDialog(defaultDay: Int = 1) {
        val firstSubId = subjects.value.firstOrNull()?.id
        _uiState.update {
            it.copy(
                showAddEditScheduleDialog = true,
                editingSchedule = null,
                scheduleDayOfWeek = defaultDay,
                scheduleStartHour = 8,
                scheduleStartMinute = 0,
                scheduleEndHour = 10,
                scheduleEndMinute = 0,
                scheduleSubjectId = firstSubId,
                scheduleTopicInput = "",
                scheduleLabelInput = "",
                scheduleError = null
            )
        }
    }

    fun openEditScheduleDialog(schedule: ScheduleWithSubject) {
        _uiState.update {
            it.copy(
                showAddEditScheduleDialog = true,
                editingSchedule = schedule,
                scheduleDayOfWeek = schedule.dayOfWeek,
                scheduleStartHour = schedule.startHour,
                scheduleStartMinute = schedule.startMinute,
                scheduleEndHour = schedule.endHour,
                scheduleEndMinute = schedule.endMinute,
                scheduleSubjectId = schedule.subjectId,
                scheduleTopicInput = schedule.topic,
                scheduleLabelInput = schedule.label,
                scheduleError = null
            )
        }
    }

    fun closeScheduleDialog() {
        _uiState.update { it.copy(showAddEditScheduleDialog = false, scheduleError = null) }
    }

    fun updateScheduleForm(
        dayOfWeek: Int = _uiState.value.scheduleDayOfWeek,
        startHour: Int = _uiState.value.scheduleStartHour,
        startMinute: Int = _uiState.value.scheduleStartMinute,
        endHour: Int = _uiState.value.scheduleEndHour,
        endMinute: Int = _uiState.value.scheduleEndMinute,
        subjectId: Long? = _uiState.value.scheduleSubjectId,
        topic: String = _uiState.value.scheduleTopicInput,
        label: String = _uiState.value.scheduleLabelInput
    ) {
        _uiState.update {
            it.copy(
                scheduleDayOfWeek = dayOfWeek,
                scheduleStartHour = startHour,
                scheduleStartMinute = startMinute,
                scheduleEndHour = endHour,
                scheduleEndMinute = endMinute,
                scheduleSubjectId = subjectId,
                scheduleTopicInput = topic,
                scheduleLabelInput = label,
                scheduleError = null
            )
        }
    }

    fun saveSchedule() {
        val subId = _uiState.value.scheduleSubjectId
        if (subId == null) {
            _uiState.update { it.copy(scheduleError = "Selecione uma matéria para o horário") }
            return
        }

        val startH = _uiState.value.scheduleStartHour
        val startM = _uiState.value.scheduleStartMinute
        val endH = _uiState.value.scheduleEndHour
        val endM = _uiState.value.scheduleEndMinute

        val startTotal = startH * 60 + startM
        val endTotal = endH * 60 + endM
        if (endTotal <= startTotal) {
            _uiState.update { it.copy(scheduleError = "O horário de término deve ser após o de início") }
            return
        }

        val activeAgendaId = _uiState.value.selectedAgendaId ?: agendas.value.firstOrNull()?.id ?: 1L

        val entity = StudyScheduleEntity(
            id = _uiState.value.editingSchedule?.id ?: 0L,
            agendaId = _uiState.value.editingSchedule?.agendaId ?: activeAgendaId,
            dayOfWeek = _uiState.value.scheduleDayOfWeek,
            startHour = startH,
            startMinute = startM,
            endHour = endH,
            endMinute = endM,
            subjectId = subId,
            topic = _uiState.value.scheduleTopicInput.trim(),
            label = _uiState.value.scheduleLabelInput.trim()
        )

        viewModelScope.launch {
            if (_uiState.value.editingSchedule != null) {
                repository.updateSchedule(entity)
            } else {
                repository.createSchedule(entity)
            }
            closeScheduleDialog()
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            repository.deleteScheduleById(id)
        }
    }
}

class SubjectsViewModelFactory(
    private val repository: StudyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
