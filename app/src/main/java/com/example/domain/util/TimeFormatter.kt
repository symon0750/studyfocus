package com.example.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utilitários puros de formatação de tempo para evitar duplicações
 * e alocações repetitivas na interface.
 */
object TimeFormatter {

    fun formatDuration(totalSeconds: Long): String {
        val totalMinutes = totalSeconds / 60
        return if (totalMinutes >= 60) {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            "${hours}h ${minutes}m"
        } else {
            val seconds = totalSeconds % 60
            "${totalMinutes}m ${seconds}s"
        }
    }

    fun formatSecondsToTimer(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    fun formatDateShort(epochMs: Long, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat("dd MMM, HH:mm", locale)
        return formatter.format(Date(epochMs))
    }

    fun formatDateFull(epochMs: Long, locale: Locale = Locale("pt", "BR")): String {
        val formatter = SimpleDateFormat("EEEE, dd 'de' MMMM 'às' HH:mm", locale)
        return formatter.format(Date(epochMs))
    }
}
