package com.example.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.vector.ImageVector

enum class StudyType(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val emoji: String = ""
) {
    READING("READING", "Leitura", Icons.Default.MenuBook),
    EXERCISES("EXERCISES", "Questões", Icons.Default.Assignment),
    VIDEO("VIDEO", "Vídeo", Icons.Default.Videocam),
    REVIEW("REVIEW", "Revisão", Icons.Default.Autorenew);

    companion object {
        fun fromId(id: String?): StudyType {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: READING
        }
    }
}
