package com.example.domain

import com.example.data.local.dao.TopicSessionSummaryDto
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.data.repository.ReviewStatus
import com.example.domain.util.ReviewScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ReviewSchedulerTest {

    private val testSubject = SubjectEntity(id = 1L, name = "Matemática", colorHex = "#3B82F6")
    private val testTopic = TopicEntity(id = 10L, name = "Álgebra Linear", subjectId = 1L)

    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 10, 14)
    private val nowMs = today.atStartOfDay(zone).toInstant().toEpochMilli() + 12 * 3600 * 1000L // 12:00 UTC

    @Test
    fun calculateTopicReviewItem_lessThanTwoPomodorosAndNoReviews_returnsNull() {
        val statsZero = TopicSessionSummaryDto(
            topicKey = "álgebra linear",
            completedPomodorosCount = 0,
            lastStudyDateEpochMs = null,
            reviewCount = 0,
            lastReviewDateEpochMs = null
        )
        assertNull(ReviewScheduler.calculateTopicReviewItem(testTopic, testSubject, statsZero, today, nowMs, zone))

        val statsOne = statsZero.copy(completedPomodorosCount = 1, lastStudyDateEpochMs = nowMs - 3600000L)
        assertNull(ReviewScheduler.calculateTopicReviewItem(testTopic, testSubject, statsOne, today, nowMs, zone))
    }

    @Test
    fun calculateTopicReviewItem_twoPomodorosCompleted_schedulesFirstReviewIn24Hours() {
        val lastStudyTime = nowMs - 3600000L // 1 hora atrás
        val stats = TopicSessionSummaryDto(
            topicKey = "álgebra linear",
            completedPomodorosCount = 2,
            lastStudyDateEpochMs = lastStudyTime,
            reviewCount = 0,
            lastReviewDateEpochMs = null
        )

        val item = ReviewScheduler.calculateTopicReviewItem(testTopic, testSubject, stats, today, nowMs, zone)
        assertNotNull(item)
        item!!

        assertEquals(0, item.reviewCount)
        assertEquals(2, item.completedPomodorosCount)
        assertEquals(1, item.intervalDays) // 1 dia (24h)
        assertEquals(lastStudyTime + ReviewScheduler.ONE_DAY_MS, item.nextReviewEpochMs)
        assertEquals(ReviewStatus.UPCOMING, item.status)
    }

    @Test
    fun calculateTopicReviewItem_firstReviewDone7DaysAgo_statusIsReady() {
        val sevenDaysAgo = nowMs - (7 * ReviewScheduler.ONE_DAY_MS)
        val stats = TopicSessionSummaryDto(
            topicKey = "álgebra linear",
            completedPomodorosCount = 2,
            lastStudyDateEpochMs = sevenDaysAgo - 3600000L,
            reviewCount = 1,
            lastReviewDateEpochMs = sevenDaysAgo
        )

        val item = ReviewScheduler.calculateTopicReviewItem(testTopic, testSubject, stats, today, nowMs, zone)
        assertNotNull(item)
        item!!

        assertEquals(1, item.reviewCount)
        assertEquals(7, item.intervalDays) // 7 dias após a 1ª revisão
        assertEquals(ReviewStatus.READY, item.status)
        assertEquals(0L, item.daysDiff)
    }

    @Test
    fun calculateTopicReviewItem_secondReviewDone_schedules15DaysInterval() {
        val twoDaysAgo = nowMs - (2 * ReviewScheduler.ONE_DAY_MS)
        val stats = TopicSessionSummaryDto(
            topicKey = "álgebra linear",
            completedPomodorosCount = 2,
            lastStudyDateEpochMs = twoDaysAgo - 10000000L,
            reviewCount = 2,
            lastReviewDateEpochMs = twoDaysAgo
        )

        val item = ReviewScheduler.calculateTopicReviewItem(testTopic, testSubject, stats, today, nowMs, zone)
        assertNotNull(item)
        item!!

        assertEquals(2, item.reviewCount)
        assertEquals(15, item.intervalDays) // 15 dias após a 2ª revisão
        assertEquals(twoDaysAgo + (15 * ReviewScheduler.ONE_DAY_MS), item.nextReviewEpochMs)
        assertEquals(ReviewStatus.UPCOMING, item.status)
        assertEquals(13L, item.daysDiff)
    }

    @Test
    fun calculateTopicReviewItem_thirdReviewDone_schedules30DaysInterval() {
        val stats = TopicSessionSummaryDto(
            topicKey = "álgebra linear",
            completedPomodorosCount = 2,
            lastStudyDateEpochMs = nowMs - 50000000L,
            reviewCount = 3,
            lastReviewDateEpochMs = nowMs
        )

        val item = ReviewScheduler.calculateTopicReviewItem(testTopic, testSubject, stats, today, nowMs, zone)
        assertNotNull(item)
        item!!

        assertEquals(3, item.reviewCount)
        assertEquals(30, item.intervalDays) // 30 dias após a 3ª revisão
        assertEquals(ReviewStatus.UPCOMING, item.status)
    }

    @Test
    fun calculatePendingPomodoroItem_onePomodoroAwaitingSecond_returnsPendingItem() {
        val statsOne = TopicSessionSummaryDto(
            topicKey = "álgebra linear",
            completedPomodorosCount = 1,
            lastStudyDateEpochMs = nowMs,
            reviewCount = 0,
            lastReviewDateEpochMs = null
        )

        val pending = ReviewScheduler.calculatePendingPomodoroItem(testTopic, testSubject, statsOne)
        assertNotNull(pending)
        assertEquals("Álgebra Linear", pending?.topicName)
        assertEquals(1, pending?.completedPomodoros)
    }

    @Test
    fun calculatePendingPomodoroItem_twoPomodorosCompleted_returnsNull() {
        val statsTwo = TopicSessionSummaryDto(
            topicKey = "álgebra linear",
            completedPomodorosCount = 2,
            lastStudyDateEpochMs = nowMs,
            reviewCount = 0,
            lastReviewDateEpochMs = null
        )

        assertNull(ReviewScheduler.calculatePendingPomodoroItem(testTopic, testSubject, statsTwo))
    }
}
