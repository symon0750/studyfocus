package com.example.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.SessionWithSubject
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.data.repository.StudyRepository
import com.example.domain.model.StudyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val filterSubjectId: Long? = null,
    val selectedSessionForDetails: SessionWithSubject? = null,
    val showManualDialog: Boolean = false,
    val manualTopicInput: String = "",
    val manualSubjectSelected: SubjectEntity? = null,
    val manualStudyType: StudyType = StudyType.READING,
    val manualDurationMinutes: Int = 30,
    val manualNotes: String = "",
    val manualError: String? = null,
    val isManualTopicAutoLinked: Boolean = false
)

class HistoryViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

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

    val rawSessions: StateFlow<List<SessionWithSubject>> = repository.getRecentSessions(limit = 200)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredSessions: StateFlow<List<SessionWithSubject>> = combine(rawSessions, _uiState) { list, state ->
        if (state.filterSubjectId == null) {
            list
        } else {
            list.filter { it.subjectId == state.filterSubjectId }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilterSubject(subjectId: Long?) {
        _uiState.update { it.copy(filterSubjectId = subjectId) }
    }

    fun selectSessionForDetails(session: SessionWithSubject?) {
        _uiState.update { it.copy(selectedSessionForDetails = session) }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.selectedSessionForDetails?.id == sessionId) {
                _uiState.update { it.copy(selectedSessionForDetails = null) }
            }
        }
    }

    // --- REGISTRO MANUAL DE ESTUDOS ---

    fun openManualDialog() {
        _uiState.update {
            it.copy(
                showManualDialog = true,
                manualTopicInput = "",
                manualSubjectSelected = null,
                manualStudyType = StudyType.READING,
                manualDurationMinutes = 30,
                manualNotes = "",
                manualError = null,
                isManualTopicAutoLinked = false
            )
        }
    }

    fun closeManualDialog() {
        _uiState.update { it.copy(showManualDialog = false, manualError = null) }
    }

    fun onManualTopicChange(topicText: String) {
        _uiState.update {
            it.copy(
                manualTopicInput = topicText,
                manualError = null
            )
        }
        viewModelScope.launch {
            val trimmed = topicText.trim()
            if (trimmed.isNotEmpty()) {
                val matchedSubject = repository.getSubjectForTopic(trimmed)
                if (matchedSubject != null) {
                    _uiState.update {
                        it.copy(
                            manualSubjectSelected = matchedSubject,
                            isManualTopicAutoLinked = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(isManualTopicAutoLinked = false) }
                }
            } else {
                _uiState.update { it.copy(isManualTopicAutoLinked = false) }
            }
        }
    }

    fun selectManualTopicFromList(topic: TopicEntity) {
        _uiState.update {
            it.copy(
                manualTopicInput = topic.name,
                manualError = null
            )
        }
        viewModelScope.launch {
            val matchedSubject = repository.getSubjectById(topic.subjectId)
            if (matchedSubject != null) {
                _uiState.update {
                    it.copy(
                        manualSubjectSelected = matchedSubject,
                        isManualTopicAutoLinked = true
                    )
                }
            }
        }
    }

    fun onManualSubjectSelect(subject: SubjectEntity) {
        _uiState.update {
            it.copy(
                manualSubjectSelected = subject,
                isManualTopicAutoLinked = false,
                manualError = null
            )
        }
    }

    fun onManualStudyTypeSelect(type: StudyType) {
        _uiState.update { it.copy(manualStudyType = type) }
    }

    fun onManualDurationChange(minutes: Int) {
        _uiState.update { it.copy(manualDurationMinutes = minutes.coerceIn(1, 600)) }
    }

    fun onManualNotesChange(notes: String) {
        _uiState.update { it.copy(manualNotes = notes) }
    }

    fun saveManualSession() {
        val topic = _uiState.value.manualTopicInput.trim()
        val subject = _uiState.value.manualSubjectSelected
        val durationMin = _uiState.value.manualDurationMinutes

        if (topic.isEmpty()) {
            _uiState.update { it.copy(manualError = "Informe o assunto do estudo.") }
            return
        }

        if (subject == null) {
            _uiState.update { it.copy(manualError = "Selecione a matéria correspondente.") }
            return
        }

        viewModelScope.launch {
            repository.recordManualSession(
                subjectId = subject.id,
                topic = topic,
                studyType = _uiState.value.manualStudyType.id,
                durationMinutes = durationMin,
                startTimeEpochMs = System.currentTimeMillis() - (durationMin * 60 * 1000L),
                notes = _uiState.value.manualNotes
            )
            closeManualDialog()
        }
    }
}

class HistoryViewModelFactory(
    private val repository: StudyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
