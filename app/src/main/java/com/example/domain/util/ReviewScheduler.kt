package com.example.domain.util

import com.example.data.local.dao.TopicSessionSummaryDto
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.data.repository.ReviewStatus
import com.example.data.repository.TopicPendingPomodoroItem
import com.example.data.repository.TopicReviewItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Funções puras para o cálculo e agendamento de revisões espaçadas e tópicos pendentes.
 *
 * Regras de Negócio:
 * 1. Agendamento automático ativado quando o tópico acumula 2 pomodoros completos de estudo,
 *    ou caso já possua histórico prévio de revisões.
 * 2. Intervalos de revisão baseados na ÚLTIMA sessão de revisão realizada:
 *    - 1ª revisão realizada -> próxima em 7 dias
 *    - 2ª revisão realizada -> próxima em 15 dias
 *    - 3ª revisão realizada ou mais -> periódica a cada 30 dias
 *    - Nenhuma revisão realizada ainda (recém-completou 2 pomodoros) -> 1ª revisão em 24h (1 dia)
 * 3. Sem conceito de atraso destrutivo (OVERDUE): a revisão fica READY (disponível)
 *    para o usuário revisar quando puder, e a próxima será contada a partir da nova data.
 */
object ReviewScheduler {

    const val ONE_DAY_MS = 24L * 60 * 60 * 1000L

    fun getNextIntervalDays(reviewCount: Int, hasLastReview: Boolean): Int {
        return if (hasLastReview) {
            when (reviewCount) {
                1 -> 7
                2 -> 15
                else -> 30
            }
        } else {
            1
        }
    }

    fun calculateTopicReviewItem(
        topic: TopicEntity,
        subject: SubjectEntity?,
        stats: TopicSessionSummaryDto?,
        today: LocalDate = LocalDate.now(),
        nowMs: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): TopicReviewItem? {
        val completedPomodorosCount = stats?.completedPomodorosCount ?: 0
        val reviewCount = stats?.reviewCount ?: 0

        // Regra: exige ao menos 2 pomodoros concluídos de estudo OU revisão prévia
        if (completedPomodorosCount < 2 && reviewCount == 0) {
            return null
        }

        val lastReviewDateEpochMs = stats?.lastReviewDateEpochMs
        val lastStudyDateEpochMs = stats?.lastStudyDateEpochMs

        val intervalDays = getNextIntervalDays(
            reviewCount = reviewCount,
            hasLastReview = lastReviewDateEpochMs != null
        )

        val baseEpochMs = lastReviewDateEpochMs
            ?: lastStudyDateEpochMs
            ?: topic.lastUsedEpochMs

        val nextReviewEpochMs = baseEpochMs + (intervalDays * ONE_DAY_MS)
        val nextDate = Instant.ofEpochMilli(nextReviewEpochMs).atZone(zone).toLocalDate()
        val daysDiff = ChronoUnit.DAYS.between(today, nextDate)

        val status = if (nowMs >= nextReviewEpochMs || daysDiff <= 0L) {
            ReviewStatus.READY
        } else {
            ReviewStatus.UPCOMING
        }

        return TopicReviewItem(
            topicId = topic.id,
            topicName = topic.name,
            subjectId = topic.subjectId,
            subjectName = subject?.name ?: "Sem Matéria",
            subjectColorHex = subject?.colorHex ?: "#EC4899",
            lastReviewDateEpochMs = lastReviewDateEpochMs,
            lastStudyDateEpochMs = lastStudyDateEpochMs,
            reviewCount = reviewCount,
            completedPomodorosCount = completedPomodorosCount,
            intervalDays = intervalDays,
            nextReviewEpochMs = nextReviewEpochMs,
            status = status,
            daysDiff = if (daysDiff < 0) 0 else daysDiff
        )
    }

    fun calculatePendingPomodoroItem(
        topic: TopicEntity,
        subject: SubjectEntity?,
        stats: TopicSessionSummaryDto?
    ): TopicPendingPomodoroItem? {
        val completed = stats?.completedPomodorosCount ?: 0
        val reviews = stats?.reviewCount ?: 0

        // Se tem exatamente 1 pomodoro completo e ainda nenhuma revisão, aguarda o 2º
        return if (completed == 1 && reviews == 0) {
            TopicPendingPomodoroItem(
                topicId = topic.id,
                topicName = topic.name,
                subjectId = topic.subjectId,
                subjectName = subject?.name ?: "Sem Matéria",
                subjectColorHex = subject?.colorHex ?: "#EC4899",
                completedPomodoros = 1
            )
        } else {
            null
        }
    }

    fun calculateScheduledReviews(
        topics: List<TopicEntity>,
        subjects: List<SubjectEntity>,
        summaries: List<TopicSessionSummaryDto>,
        today: LocalDate = LocalDate.now(),
        nowMs: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.systemDefault()
    ): List<TopicReviewItem> {
        val summariesMap = summaries.associateBy { it.topicKey.trim().lowercase() }
        val subjectsMap = subjects.associateBy { it.id }

        return topics.mapNotNull { topic ->
            val key = topic.name.trim().lowercase()
            val stats = summariesMap[key]
            val subject = subjectsMap[topic.subjectId]
            calculateTopicReviewItem(
                topic = topic,
                subject = subject,
                stats = stats,
                today = today,
                nowMs = nowMs,
                zone = zone
            )
        }.sortedWith(
            compareBy<TopicReviewItem> {
                when (it.status) {
                    ReviewStatus.READY -> 0
                    ReviewStatus.UPCOMING -> 1
                }
            }.thenBy { it.nextReviewEpochMs }
        )
    }

    fun calculateTopicsAwaitingPomodoros(
        topics: List<TopicEntity>,
        subjects: List<SubjectEntity>,
        summaries: List<TopicSessionSummaryDto>
    ): List<TopicPendingPomodoroItem> {
        val summariesMap = summaries.associateBy { it.topicKey.trim().lowercase() }
        val subjectsMap = subjects.associateBy { it.id }

        return topics.mapNotNull { topic ->
            val key = topic.name.trim().lowercase()
            val stats = summariesMap[key]
            val subject = subjectsMap[topic.subjectId]
            calculatePendingPomodoroItem(
                topic = topic,
                subject = subject,
                stats = stats
            )
        }
    }
}
