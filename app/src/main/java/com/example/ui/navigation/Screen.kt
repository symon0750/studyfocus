package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    POMODORO("pomodoro", "Estudar", Icons.Default.Timer),
    SUBJECTS("subjects", "Planner", Icons.Default.Book),
    STATS_HISTORY("stats_history", "Dados", Icons.Default.BarChart),
    BLOCKER("blocker", "Foco", Icons.Default.Shield)
}

