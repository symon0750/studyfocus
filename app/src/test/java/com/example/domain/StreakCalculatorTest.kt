package com.example.domain

import com.example.domain.util.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {

    private val referenceToday = LocalDate.of(2026, 10, 14)

    @Test
    fun testEmptyDays_returnsZero() {
        assertEquals(0, StreakCalculator.calculateStreak(emptyList(), referenceToday))
    }

    @Test
    fun testActiveStreakStudiedToday() {
        val days = listOf("2026-10-14", "2026-10-13", "2026-10-12")
        assertEquals(3, StreakCalculator.calculateStreak(days, referenceToday))
    }

    @Test
    fun testActiveStreakStudiedYesterdayNotTodayYet() {
        // O usuário estudou ontem, ainda tem chance de manter hoje
        val days = listOf("2026-10-13", "2026-10-12")
        assertEquals(2, StreakCalculator.calculateStreak(days, referenceToday))
    }

    @Test
    fun testBrokenStreakLastStudiedTwoDaysAgo() {
        val days = listOf("2026-10-12", "2026-10-11")
        assertEquals(0, StreakCalculator.calculateStreak(days, referenceToday))
    }

    @Test
    fun testDuplicateDaysAndUnorderedInput() {
        val days = listOf("2026-10-13", "2026-10-14", "2026-10-14", "2026-10-12", "2026-10-13")
        assertEquals(3, StreakCalculator.calculateStreak(days, referenceToday))
    }

    @Test
    fun testFutureAnomalyIgnored() {
        // Se houver uma anomalia com data no futuro em relação ao referenceToday
        val days = listOf("2026-10-16", "2026-10-14", "2026-10-13")
        assertEquals(2, StreakCalculator.calculateStreak(days, referenceToday))
    }

    @Test
    fun testInvalidDateStringsIgnoredGracefully() {
        val days = listOf("invalid-date", "2026-10-14", "", "null", "2026-10-13")
        assertEquals(2, StreakCalculator.calculateStreak(days, referenceToday))
    }
}
