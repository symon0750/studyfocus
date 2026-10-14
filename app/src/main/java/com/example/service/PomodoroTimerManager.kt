package com.example.service

import com.example.domain.model.PomodoroPhase
import com.example.domain.model.TimerState
import com.example.domain.model.TimerTickState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PomodoroTimerManager(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private var timerJob: Job? = null

    private val _timerState = MutableStateFlow(TimerTickState())
    val timerState: StateFlow<TimerTickState> = _timerState.asStateFlow()

    fun startOrResume(
        targetMinutes: Int = _timerState.value.phase.defaultMinutes,
        phase: PomodoroPhase? = null
    ) {
        val current = _timerState.value

        if (current.state == TimerState.PAUSED) {
            resume()
            return
        }

        val targetPhase = phase ?: current.phase
        val totalSecs = targetMinutes * 60

        _timerState.update {
            it.copy(
                state = TimerState.RUNNING,
                phase = targetPhase,
                totalSeconds = totalSecs,
                remainingSeconds = totalSecs,
                progress = 0f,
                sessionStartEpochMs = System.currentTimeMillis(),
                elapsedSeconds = 0
            )
        }

        startTicker()
    }

    fun pause() {
        if (_timerState.value.state != TimerState.RUNNING) return
        timerJob?.cancel()
        timerJob = null
        _timerState.update { it.copy(state = TimerState.PAUSED) }
    }

    fun resume() {
        if (_timerState.value.state != TimerState.PAUSED) return
        _timerState.update { it.copy(state = TimerState.RUNNING) }
        startTicker()
    }

    fun stopAndReset() {
        timerJob?.cancel()
        timerJob = null
        val currentPhase = _timerState.value.phase
        val defaultSecs = currentPhase.defaultMinutes * 60
        _timerState.update {
            it.copy(
                state = TimerState.IDLE,
                remainingSeconds = defaultSecs,
                totalSeconds = defaultSecs,
                progress = 0f,
                elapsedSeconds = 0
            )
        }
    }

    fun setPhase(phase: PomodoroPhase, targetMinutes: Int = phase.defaultMinutes) {
        timerJob?.cancel()
        timerJob = null
        val totalSecs = targetMinutes * 60
        _timerState.update {
            it.copy(
                state = TimerState.IDLE,
                phase = phase,
                remainingSeconds = totalSecs,
                totalSeconds = totalSecs,
                progress = 0f,
                elapsedSeconds = 0
            )
        }
    }

    fun skipToNextPhase() {
        timerJob?.cancel()
        timerJob = null

        val current = _timerState.value
        val (nextPhase, nextCycle) = when (current.phase) {
            PomodoroPhase.FOCUS -> {
                if (current.currentCycle >= current.totalCyclesInSet) {
                    PomodoroPhase.LONG_BREAK to 1
                } else {
                    PomodoroPhase.SHORT_BREAK to current.currentCycle
                }
            }
            PomodoroPhase.SHORT_BREAK -> {
                PomodoroPhase.FOCUS to (current.currentCycle + 1)
            }
            PomodoroPhase.LONG_BREAK -> {
                PomodoroPhase.FOCUS to 1
            }
        }

        val nextSecs = nextPhase.defaultMinutes * 60
        _timerState.update {
            it.copy(
                state = TimerState.IDLE,
                phase = nextPhase,
                remainingSeconds = nextSecs,
                totalSeconds = nextSecs,
                progress = 0f,
                currentCycle = nextCycle,
                elapsedSeconds = 0
            )
        }
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (true) {
                delay(1000L)
                val current = _timerState.value
                if (current.state != TimerState.RUNNING) break

                val newRemaining = current.remainingSeconds - 1
                val newElapsed = current.elapsedSeconds + 1

                if (newRemaining <= 0) {
                    val progress = 1.0f
                    _timerState.update {
                        it.copy(
                            state = TimerState.COMPLETED,
                            remainingSeconds = 0,
                            elapsedSeconds = newElapsed,
                            progress = progress
                        )
                    }
                    break
                } else {
                    val progress = 1f - (newRemaining.toFloat() / current.totalSeconds.toFloat())
                    _timerState.update {
                        it.copy(
                            remainingSeconds = newRemaining,
                            elapsedSeconds = newElapsed,
                            progress = progress
                        )
                    }
                }
            }
        }
    }
}
