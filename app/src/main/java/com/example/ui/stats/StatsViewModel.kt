package com.example.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.dao.DailySummaryDto
import com.example.data.local.dao.SubjectStatDto
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(
    private val repository: StudyRepository
) : ViewModel() {

    val todaySummary: StateFlow<DailySummaryDto> = repository.getTodaySummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailySummaryDto(0, 0, 0)
        )

    val allTimeStats: StateFlow<DailySummaryDto> = repository.getAllTimeStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailySummaryDto(0, 0, 0)
        )

    val subjectStats: StateFlow<List<SubjectStatDto>> = repository.getSubjectStats(daysLookback = 30)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentStreakDays: StateFlow<Int> = repository.getCurrentStreakDays()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}

class StatsViewModelFactory(
    private val repository: StudyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
