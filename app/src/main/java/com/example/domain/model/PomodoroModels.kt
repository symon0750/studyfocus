package com.example.domain.model

enum class PomodoroPhase(val label: String, val defaultMinutes: Int) {
    FOCUS("Foco", 25),
    SHORT_BREAK("Pausa Curta", 5),
    LONG_BREAK("Pausa Longa", 15)
}

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

data class TimerTickState(
    val state: TimerState = TimerState.IDLE,
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val remainingSeconds: Int = PomodoroPhase.FOCUS.defaultMinutes * 60,
    val totalSeconds: Int = PomodoroPhase.FOCUS.defaultMinutes * 60,
    val progress: Float = 0f,
    val currentCycle: Int = 1,
    val totalCyclesInSet: Int = 4,
    val sessionStartEpochMs: Long = 0L,
    val elapsedSeconds: Int = 0
)
