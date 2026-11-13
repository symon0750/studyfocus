package com.example.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.dao.SessionWithSubject
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.domain.model.StudyType
import com.example.ui.theme.BreakGreen
import com.example.ui.theme.PomodoroRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    val sessions by viewModel.filteredSessions.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Cabeçalho com Título e Botão de Registro Manual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Histórico",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${sessions.size} sessões",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Surface(
                shape = CircleShape,
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                modifier = Modifier.size(36.dp)
            ) {
                IconButton(
                    onClick = { viewModel.openManualDialog() },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("add_manual_session_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Registrar Estudo Manual",
                        tint = PomodoroRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Barra de Filtros por Disciplina
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.filterSubjectId == null,
                onClick = { viewModel.setFilterSubject(null) },
                label = { Text("Todas") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PomodoroRed,
                    selectedLabelColor = Color.White
                )
            )

            subjects.forEach { subject ->
                val isSelected = uiState.filterSubjectId == subject.id
                val chipColor = try {
                    Color(android.graphics.Color.parseColor(subject.colorHex))
                } catch (_: Exception) {
                    PomodoroRed
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setFilterSubject(if (isSelected) null else subject.id) },
                    label = { Text(subject.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(chipColor)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Sessões ou Estado Vazio
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhuma sessão registrada",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Inicie um ciclo de foco no Pomodoro ou toque no botão de adicionar acima para registrar manualmente.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = sessions,
                    key = { it.id }
                ) { session ->
                    SessionItemCard(
                        session = session,
                        onClick = { viewModel.selectSessionForDetails(session) }
                    )
                }
            }
        }
    }

    // Modal de Detalhes da Sessão
    uiState.selectedSessionForDetails?.let { session ->
        SessionDetailsDialog(
            session = session,
            onDismiss = { viewModel.selectSessionForDetails(null) },
            onDelete = { viewModel.deleteSession(session.id) }
        )
    }

    // MODAL DE REGISTRO MANUAL DE ESTUDOS
    if (uiState.showManualDialog) {
        ManualStudySessionDialog(
            topicInput = uiState.manualTopicInput,
            selectedSubject = uiState.manualSubjectSelected,
            selectedStudyType = uiState.manualStudyType,
            durationMinutes = uiState.manualDurationMinutes,
            notes = uiState.manualNotes,
            subjects = subjects,
            topics = topics,
            errorMessage = uiState.manualError,
            isAutoLinked = uiState.isManualTopicAutoLinked,
            onTopicChange = { viewModel.onManualTopicChange(it) },
            onTopicSelectedFromList = { viewModel.selectManualTopicFromList(it) },
            onSubjectSelected = { viewModel.onManualSubjectSelect(it) },
            onStudyTypeSelected = { viewModel.onManualStudyTypeSelect(it) },
            onDurationChange = { viewModel.onManualDurationChange(it) },
            onNotesChange = { viewModel.onManualNotesChange(it) },
            onDismiss = { viewModel.closeManualDialog() },
            onSave = { viewModel.saveManualSession() }
        )
    }
}

@Composable
fun SessionItemCard(
    session: SessionWithSubject,
    onClick: () -> Unit
) {
    val subjectColor = session.subjectColor?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { PomodoroRed }
    } ?: PomodoroRed

    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date(session.startTimeEpochMs))

    val actualMinutes = session.actualDurationSeconds / 60
    val actualSeconds = session.actualDurationSeconds % 60
    val durationText = if (actualMinutes >= 60) {
        val h = actualMinutes / 60
        val m = actualMinutes % 60
        "${h}h ${m}m"
    } else {
        "${actualMinutes}m ${actualSeconds}s"
    }

    val studyType = StudyType.fromId(session.studyType)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("session_item_${session.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Assunto em destaque
                Text(
                    text = session.topic.ifEmpty { "Estudo Geral" },
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Matéria e Tipo de Estudo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(subjectColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = session.subjectName ?: "Geral",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${studyType.emoji} ${studyType.label}",
                        color = PomodoroRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formattedDate,
                    color = TextMuted,
                    fontSize = 11.sp
                )

                if (session.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = session.notes,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = durationText,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.PauseCircle,
                        contentDescription = null,
                        tint = if (session.completed) BreakGreen else Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (session.completed) "Concluído" else "Interrompido",
                        color = if (session.completed) BreakGreen else Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ManualStudySessionDialog(
    topicInput: String,
    selectedSubject: SubjectEntity?,
    selectedStudyType: StudyType,
    durationMinutes: Int,
    notes: String,
    subjects: List<SubjectEntity>,
    topics: List<TopicEntity>,
    errorMessage: String?,
    isAutoLinked: Boolean,
    onTopicChange: (String) -> Unit,
    onTopicSelectedFromList: (TopicEntity) -> Unit,
    onSubjectSelected: (SubjectEntity) -> Unit,
    onStudyTypeSelected: (StudyType) -> Unit,
    onDurationChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var customDuration by remember { mutableFloatStateOf(durationMinutes.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Registrar Estudo Manual",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Lance uma sessão de estudo realizada fora do app",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Duração do Estudo
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tempo Estudado", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("${customDuration.toInt()} min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PomodoroRed)
                    }
                    Slider(
                        value = customDuration,
                        onValueChange = {
                            customDuration = it
                            onDurationChange(it.toInt())
                        },
                        valueRange = 5f..180f,
                        steps = 34
                    )
                    // Atalhos rápidos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(25, 45, 60, 90).forEach { mins ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                modifier = Modifier.clickable {
                                    customDuration = mins.toFloat()
                                    onDurationChange(mins)
                                }
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Assunto (Tópico com autocompletar)
                Column {
                    Text("Assunto Estudado", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = onTopicChange,
                        placeholder = { Text("Ex: Genética Mendeliana") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("manual_topic_input"),
                        singleLine = true
                    )

                    if (topics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            topics.take(6).forEach { topicItem ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceCard,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                                    modifier = Modifier.clickable { onTopicSelectedFromList(topicItem) }
                                ) {
                                    Text(
                                        text = topicItem.name,
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Matéria Vinculada
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Matéria Correspondente", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        if (isAutoLinked) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BreakGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto Vinculada", fontSize = 11.sp, color = BreakGreen, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.forEach { subject ->
                            val isSelected = selectedSubject?.id == subject.id
                            val color = try {
                                Color(android.graphics.Color.parseColor(subject.colorHex))
                            } catch (_: Exception) {
                                PomodoroRed
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) color.copy(alpha = 0.2f) else SurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) color else SurfaceCardBorder
                                ),
                                modifier = Modifier
                                    .clickable { onSubjectSelected(subject) }
                                    .testTag("manual_subject_${subject.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = subject.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) TextPrimary else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Tipo de Estudo
                Column {
                    Text("Tipo de Estudo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudyType.values().forEach { type ->
                            val isSelected = selectedStudyType == type
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) PomodoroRed.copy(alpha = 0.18f) else SurfaceCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PomodoroRed else SurfaceCardBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onStudyTypeSelected(type) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = type.emoji, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = type.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PomodoroRed else TextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Anotações
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Anotações da sessão (opcional)") },
                    placeholder = { Text("Ex: Leitura dos capítulos 1 e 2") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = PomodoroRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                modifier = Modifier.testTag("save_manual_session_button")
            ) {
                Text("Salvar Sessão")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
fun SessionDetailsDialog(
    session: SessionWithSubject,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("EEEE, dd 'de' MMMM 'às' HH:mm", Locale("pt", "BR")) }
    val formattedDate = dateFormatter.format(Date(session.startTimeEpochMs))
    val studyType = StudyType.fromId(session.studyType)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Detalhes da Sessão",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Assunto: ${session.topic.ifEmpty { "Geral" }}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Disciplina: ${session.subjectName ?: "Geral"}", color = TextSecondary, fontSize = 14.sp)
                Text("Tipo de Estudo: ${studyType.emoji} ${studyType.label}", color = PomodoroRed, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text("Data: $formattedDate", color = TextSecondary, fontSize = 13.sp)
                Text("Duração: ${session.actualDurationSeconds / 60} minutos", color = TextSecondary, fontSize = 13.sp)
                Text("Status: ${if (session.completed) "Ciclo Completo" else "Interrompido"}", color = if (session.completed) BreakGreen else Color(0xFFF59E0B), fontSize = 13.sp)
                if (session.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Anotações:", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(session.notes, color = TextSecondary, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = TextPrimary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = PomodoroRed)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Excluir")
            }
        }
    )
}
