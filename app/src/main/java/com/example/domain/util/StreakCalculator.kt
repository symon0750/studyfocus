package com.example.domain.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Função pura para o cálculo de sequência diária (streak) de estudos.
 *
 * Propriedades:
 * - Determinística e sem efeitos colaterais.
 * - Tolera datas desordenadas, formatos inválidos ou duplicatas.
 * - Trata anomalias temporais (ex: data futura gerada por ajuste de fuso horário).
 */
object StreakCalculator {

    fun calculateStreak(studyDays: List<String>, today: LocalDate = LocalDate.now()): Int {
        if (studyDays.isEmpty()) return 0

        val parsedDays = studyDays.mapNotNull { raw ->
            try {
                LocalDate.parse(raw.trim())
            } catch (_: Exception) {
                null
            }
        }
            // Filtra eventuais anomalias de datas futuras em relação a 'today'
            .filter { !it.isAfter(today) }
            .distinct()
            .sortedDescending()

        if (parsedDays.isEmpty()) return 0

        val mostRecent = parsedDays.first()
        val diffFromToday = ChronoUnit.DAYS.between(mostRecent, today)

        // Se o dia mais recente for anterior a ontem (diff > 1), o streak foi quebrado
        if (diffFromToday > 1) return 0

        var streak = 1
        for (i in 0 until parsedDays.size - 1) {
            val current = parsedDays[i]
            val previous = parsedDays[i + 1]
            if (ChronoUnit.DAYS.between(previous, current) == 1L) {
                streak++
            } else {
                break
            }
        }
        return streak
    }
}
