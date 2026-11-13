package com.example.data.local.dao

data class DailySummaryDto(
    val totalSeconds: Long,
    val totalSessions: Int,
    val completedSessions: Int
)

data class SubjectStatDto(
    val subjectId: Long?,
    val subjectName: String?,
    val colorHex: String?,
    val totalDurationSeconds: Long,
    val sessionCount: Int
)

data class SessionWithSubject(
    val id: Long,
    val subjectId: Long?,
    val subjectName: String?,
    val subjectColor: String?,
    val topic: String,
    val studyType: String,
    val phaseType: String,
    val targetDurationSeconds: Int,
    val actualDurationSeconds: Int,
    val startTimeEpochMs: Long,
    val endTimeEpochMs: Long,
    val completed: Boolean,
    val notes: String
)

data class TopicSessionSummaryDto(
    val topicKey: String,
    val completedPomodorosCount: Int,
    val lastStudyDateEpochMs: Long?,
    val reviewCount: Int,
    val lastReviewDateEpochMs: Long?
)

