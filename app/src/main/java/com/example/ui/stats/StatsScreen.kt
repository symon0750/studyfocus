package com.example.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.BreakGreen
import com.example.ui.theme.LongBreakBlue
import com.example.ui.theme.PomodoroRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    val allTimeStats by viewModel.allTimeStats.collectAsStateWithLifecycle()
    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val subjectStats by viewModel.subjectStats.collectAsStateWithLifecycle()
    val streakDays by viewModel.currentStreakDays.collectAsStateWithLifecycle()

    val totalStudySeconds = allTimeStats.totalSeconds
    val totalHours = totalStudySeconds / 3600
    val totalMinutes = (totalStudySeconds % 3600) / 60

    val completionRate = if (allTimeStats.totalSessions > 0) {
        ((allTimeStats.completedSessions.toFloat() / allTimeStats.totalSessions.toFloat()) * 100).toInt()
    } else {
        0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Título
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Métricas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Card de Sequência (Streak)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = SurfaceCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AccentAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = AccentAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (streakDays > 0) "$streakDays dias seguidos" else "Inicie sua sequência",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (streakDays > 0) "Sequência ativa" else "Conclua uma sessão hoje",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Grade de Cartões Analíticos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Foco total",
                value = if (totalHours > 0) "${totalHours}h ${totalMinutes}m" else "${totalMinutes}m",
                subtitle = "",
                icon = Icons.Default.Timer,
                iconColor = PomodoroRed,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Concluídas",
                value = "${allTimeStats.completedSessions}",
                subtitle = "$completionRate% do total",
                icon = Icons.Default.CheckCircle,
                iconColor = BreakGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Distribuição de Estudo por Disciplina
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = LongBreakBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Por disciplina",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (subjectStats.isEmpty() || subjectStats.all { it.totalDurationSeconds == 0L }) {
                    Text(
                        text = "Nenhum dado registrado para as disciplinas nos últimos 30 dias.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    val maxDuration = subjectStats.maxOfOrNull { it.totalDurationSeconds } ?: 1L

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        subjectStats.forEach { stat ->
                            val color = stat.colorHex?.let {
                                try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { PomodoroRed }
                            } ?: PomodoroRed

                            val durationMin = stat.totalDurationSeconds / 60
                            val durationFormatted = if (durationMin >= 60) {
                                "${durationMin / 60}h ${durationMin % 60}m"
                            } else {
                                "${durationMin}m"
                            }
                            val progress = if (maxDuration > 0) stat.totalDurationSeconds.toFloat() / maxDuration.toFloat() else 0f

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stat.subjectName ?: "Geral",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = durationFormatted,
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = color,
                                    trackColor = SurfaceCardBorder,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}
