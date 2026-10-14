package com.example.ui.subjects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.dao.CycleItemWithSubject
import com.example.data.local.dao.ScheduleWithSubject
import com.example.data.local.entity.StudyAgendaEntity
import com.example.data.local.entity.StudyCycleEntity
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BreakGreen
import com.example.ui.theme.PomodoroRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    val cycles by viewModel.studyCycles.collectAsStateWithLifecycle()
    val cycleItems by viewModel.cycleItems.collectAsStateWithLifecycle()
    val schedules by viewModel.schedules.collectAsStateWithLifecycle()
    val agendas by viewModel.agendas.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val tabs = listOf("Disciplinas", "Ciclos", "Agenda")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Cabeçalho Principal
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Planner",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Abas Superiores
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = SurfaceDark,
            contentColor = TextPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                    color = PomodoroRed,
                    height = 2.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = uiState.selectedTab == index
                Tab(
                    selected = selected,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) PomodoroRed else TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("subjects_tab_$index")
                )
            }
        }

        // Conteúdo da Aba Ativa
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.selectedTab) {
                0 -> SubjectsAndTopicsTabContent(
                    subjects = subjects,
                    topics = topics,
                    expandedSubjectIds = uiState.expandedSubjectIds,
                    onToggleExpansion = { viewModel.toggleSubjectExpansion(it) },
                    onOpenAddSubject = { viewModel.openAddSubjectDialog() },
                    onEditSubject = { viewModel.openEditSubjectDialog(it) },
                    onDeleteSubject = { viewModel.deleteSubject(it) },
                    onAddTopic = { subjectId, topicName -> viewModel.addTopicToSubject(subjectId, topicName) },
                    onEditTopic = { viewModel.openEditTopicDialog(it) },
                    onDeleteTopic = { viewModel.deleteTopic(it) }
                )
                1 -> StudyCyclesTabContent(
                    cycles = cycles,
                    cycleItems = cycleItems,
                    subjects = subjects,
                    onOpenAddCycle = { viewModel.openAddCycleDialog() },
                    onEditCycle = { viewModel.openEditCycleDialog(it) },
                    onDeleteCycle = { viewModel.deleteCycle(it) }
                )
                2 -> SchedulesTabContent(
                    agendas = agendas,
                    selectedAgendaId = uiState.selectedAgendaId,
                    schedules = schedules,
                    subjects = subjects,
                    onSelectAgenda = { viewModel.selectAgenda(it) },
                    onOpenAddAgenda = { viewModel.openAddAgendaDialog() },
                    onEditAgenda = { viewModel.openEditAgendaDialog(it) },
                    onDeleteAgenda = { viewModel.deleteAgenda(it) },
                    onOpenAddSchedule = { defaultDay -> viewModel.openAddScheduleDialog(defaultDay) },
                    onEditSchedule = { viewModel.openEditScheduleDialog(it) },
                    onDeleteSchedule = { viewModel.deleteSchedule(it.id) }
                )
            }
        }
    }

    // Diálogos de Suporte (CRUD)
    if (uiState.showAddEditSubjectDialog) {
        AddEditSubjectDialog(
            isEditing = uiState.editingSubject != null,
            name = uiState.subjectNameInput,
            selectedColorHex = uiState.selectedColorHex,
            availableColors = viewModel.availableColors,
            errorMessage = uiState.subjectError,
            onNameChange = { viewModel.onSubjectNameChange(it) },
            onColorSelect = { viewModel.onSubjectColorSelect(it) },
            onDismiss = { viewModel.closeSubjectDialog() },
            onSave = { viewModel.saveSubject() }
        )
    }

    if (uiState.showEditTopicDialog) {
        EditTopicDialog(
            name = uiState.topicEditNameInput,
            errorMessage = uiState.topicError,
            onNameChange = { viewModel.onTopicEditNameChange(it) },
            onDismiss = { viewModel.closeEditTopicDialog() },
            onSave = { viewModel.saveTopicEdit() }
        )
    }

    if (uiState.showAddEditCycleDialog) {
        AddEditCycleDialog(
            isEditing = uiState.editingCycle != null,
            name = uiState.cycleNameInput,
            description = uiState.cycleDescriptionInput,
            draftItems = uiState.cycleItemsDraft,
            availableSubjects = subjects,
            errorMessage = uiState.cycleError,
            onNameChange = { viewModel.onCycleNameChange(it) },
            onDescriptionChange = { viewModel.onCycleDescriptionChange(it) },
            onAddSubject = { subjectId, mins -> viewModel.addSubjectToCycleDraft(subjectId, mins) },
            onRemoveSubject = { viewModel.removeSubjectFromCycleDraft(it) },
            onUpdateDuration = { subjectId, mins -> viewModel.updateCycleDraftItemDuration(subjectId, mins) },
            onDismiss = { viewModel.closeCycleDialog() },
            onSave = { viewModel.saveCycle() }
        )
    }

    if (uiState.showAddEditScheduleDialog) {
        AddEditScheduleDialog(
            isEditing = uiState.editingSchedule != null,
            dayOfWeek = uiState.scheduleDayOfWeek,
            startHour = uiState.scheduleStartHour,
            startMinute = uiState.scheduleStartMinute,
            endHour = uiState.scheduleEndHour,
            endMinute = uiState.scheduleEndMinute,
            subjectId = uiState.scheduleSubjectId,
            topic = uiState.scheduleTopicInput,
            label = uiState.scheduleLabelInput,
            availableSubjects = subjects,
            errorMessage = uiState.scheduleError,
            onUpdate = { day, sH, sM, eH, eM, sId, t, l ->
                viewModel.updateScheduleForm(day, sH, sM, eH, eM, sId, t, l)
            },
            onDismiss = { viewModel.closeScheduleDialog() },
            onSave = { viewModel.saveSchedule() }
        )
    }

    if (uiState.showAddEditAgendaDialog) {
        AddEditAgendaDialog(
            isEditing = uiState.editingAgenda != null,
            name = uiState.agendaNameInput,
            description = uiState.agendaDescriptionInput,
            errorMessage = uiState.agendaError,
            onNameChange = { viewModel.onAgendaNameChange(it) },
            onDescriptionChange = { viewModel.onAgendaDescriptionChange(it) },
            onDismiss = { viewModel.closeAgendaDialog() },
            onSave = { viewModel.saveAgenda() }
        )
    }
}

// =========================================================================
// ABA 0: Disciplinas & Assuntos (Accordion Recolhível com CRUD Integrado)
// =========================================================================

@Composable
private fun SubjectsAndTopicsTabContent(
    subjects: List<SubjectEntity>,
    topics: List<TopicEntity>,
    expandedSubjectIds: Set<Long>,
    onToggleExpansion: (Long) -> Unit,
    onOpenAddSubject: () -> Unit,
    onEditSubject: (SubjectEntity) -> Unit,
    onDeleteSubject: (SubjectEntity) -> Unit,
    onAddTopic: (Long, String) -> Unit,
    onEditTopic: (TopicEntity) -> Unit,
    onDeleteTopic: (TopicEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Suas Disciplinas (${subjects.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                FilledIconButton(
                    onClick = onOpenAddSubject,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = PomodoroRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("add_subject_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar disciplina", modifier = Modifier.size(22.dp))
                }
            }
        }

        if (subjects.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Book, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Nenhuma disciplina cadastrada.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(subjects, key = { it.id }) { subject ->
                val subjectTopics = topics.filter { it.subjectId == subject.id }
                val isExpanded = expandedSubjectIds.contains(subject.id)

                ExpandableSubjectCard(
                    subject = subject,
                    topics = subjectTopics,
                    isExpanded = isExpanded,
                    onToggleExpansion = { onToggleExpansion(subject.id) },
                    onEditSubject = { onEditSubject(subject) },
                    onDeleteSubject = { onDeleteSubject(subject) },
                    onAddTopic = { topicName -> onAddTopic(subject.id, topicName) },
                    onEditTopic = onEditTopic,
                    onDeleteTopic = onDeleteTopic
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ExpandableSubjectCard(
    subject: SubjectEntity,
    topics: List<TopicEntity>,
    isExpanded: Boolean,
    onToggleExpansion: () -> Unit,
    onEditSubject: () -> Unit,
    onDeleteSubject: () -> Unit,
    onAddTopic: (String) -> Unit,
    onEditTopic: (TopicEntity) -> Unit,
    onDeleteTopic: (TopicEntity) -> Unit
) {
    val subjectColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (_: Exception) {
        PomodoroRed
    }

    var newTopicNameInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cabeçalho da Disciplina (Clicável para expandir/recolher)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpansion() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(subjectColor.copy(alpha = 0.2f))
                        .border(1.5.dp, subjectColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = subjectColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${topics.size} assunto${if (topics.size == 1) "" else "s"}",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                IconButton(
                    onClick = onEditSubject,
                    modifier = Modifier.size(34.dp).testTag("edit_subject_${subject.id}")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onDeleteSubject,
                    modifier = Modifier.size(34.dp).testTag("delete_subject_${subject.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = TextMuted, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onToggleExpansion,
                    modifier = Modifier.size(34.dp).testTag("expand_subject_${subject.id}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Recolher" else "Expandir",
                        tint = subjectColor
                    )
                }
            }

            // Corpo Recolhível (Accordion com CRUD de Assuntos)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark.copy(alpha = 0.6f))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    HorizontalDivider(color = SurfaceCardBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Assuntos de ${subject.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = subjectColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Linha para Adicionar Assunto Rápido
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTopicNameInput,
                            onValueChange = { newTopicNameInput = it },
                            placeholder = { Text("Novo assunto...", fontSize = 12.sp, color = TextMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("new_topic_input_${subject.id}"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = subjectColor,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (newTopicNameInput.isNotBlank()) {
                                    onAddTopic(newTopicNameInput.trim())
                                    newTopicNameInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = subjectColor,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("add_topic_btn_${subject.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (topics.isEmpty()) {
                        Text(
                            text = "Nenhum assunto cadastrado ainda. Digite acima para adicionar.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        topics.forEach { topic ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCard)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("topic_row_${topic.id}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = topic.name,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                IconButton(
                                    onClick = { onEditTopic(topic) },
                                    modifier = Modifier.size(28.dp).testTag("edit_topic_${topic.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(14.dp))
                                }

                                IconButton(
                                    onClick = { onDeleteTopic(topic) },
                                    modifier = Modifier.size(28.dp).testTag("delete_topic_${topic.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = TextMuted, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// ABA 1: Ciclos de Estudo (Criação, Gestão e Distribuição Proporcional)
// =========================================================================

@Composable
private fun StudyCyclesTabContent(
    cycles: List<StudyCycleEntity>,
    cycleItems: List<CycleItemWithSubject>,
    subjects: List<SubjectEntity>,
    onOpenAddCycle: () -> Unit,
    onEditCycle: (StudyCycleEntity) -> Unit,
    onDeleteCycle: (StudyCycleEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ciclos de Estudos (${cycles.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Rotação contínua de matérias com pesos definidos",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                FilledIconButton(
                    onClick = onOpenAddCycle,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = PomodoroRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("add_cycle_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar ciclo de estudo", modifier = Modifier.size(22.dp))
                }
            }
        }

        if (cycles.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Loop, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Nenhum ciclo de estudos criado ainda.", color = TextMuted, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Toque em 'Novo Ciclo' para criar seu cronograma por rotação.", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(cycles, key = { it.id }) { cycle ->
                val itemsForThisCycle = cycleItems.filter { it.cycleId == cycle.id }
                StudyCycleCard(
                    cycle = cycle,
                    items = itemsForThisCycle,
                    onEdit = { onEditCycle(cycle) },
                    onDelete = { onDeleteCycle(cycle) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StudyCycleCard(
    cycle: StudyCycleEntity,
    items: List<CycleItemWithSubject>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val totalMinutes = items.sumOf { it.targetMinutes }.coerceAtLeast(1)
    val totalHours = totalMinutes / 60
    val totalRemainingMinutes = totalMinutes % 60
    val durationText = if (totalHours > 0) "${totalHours}h ${if (totalRemainingMinutes > 0) "${totalRemainingMinutes}m" else ""}" else "${totalMinutes}m"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cycle_card_${cycle.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PomodoroRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Loop, contentDescription = null, tint = PomodoroRed, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cycle.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${items.size} matérias • Total: $durationText",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            if (cycle.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = cycle.description, fontSize = 12.sp, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Barra Multi-Cor Proporcional do Ciclo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SurfaceDark)
            ) {
                items.forEach { item ->
                    val weight = item.targetMinutes.toFloat() / totalMinutes.toFloat()
                    val color = try {
                        Color(android.graphics.Color.parseColor(item.subjectColorHex))
                    } catch (_: Exception) {
                        PomodoroRed
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(weight.coerceAtLeast(0.01f))
                            .background(color)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Matérias com Percentual
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    val percentage = (item.targetMinutes.toFloat() / totalMinutes.toFloat() * 100).toInt()
                    val color = try {
                        Color(android.graphics.Color.parseColor(item.subjectColorHex))
                    } catch (_: Exception) {
                        PomodoroRed
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = item.subjectName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${item.targetMinutes} min ($percentage%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// ABA 2: Agendas de Horários Normais (Quadro Semanal)
// =========================================================================

@Composable
private fun SchedulesTabContent(
    agendas: List<StudyAgendaEntity>,
    selectedAgendaId: Long?,
    schedules: List<ScheduleWithSubject>,
    subjects: List<SubjectEntity>,
    onSelectAgenda: (Long) -> Unit,
    onOpenAddAgenda: () -> Unit,
    onEditAgenda: (StudyAgendaEntity) -> Unit,
    onDeleteAgenda: (Long) -> Unit,
    onOpenAddSchedule: (Int) -> Unit,
    onEditSchedule: (ScheduleWithSubject) -> Unit,
    onDeleteSchedule: (ScheduleWithSubject) -> Unit
) {
    val dayNames = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom")
    var selectedDay by remember { mutableIntStateOf(1) } // 1 = Seg ... 7 = Dom

    val activeAgenda = agendas.find { it.id == selectedAgendaId } ?: agendas.firstOrNull()
    val targetAgendaId = activeAgenda?.id ?: 1L

    val daySchedules = remember(schedules, targetAgendaId, selectedDay) {
        schedules.filter {
            (it.agendaId == targetAgendaId || (targetAgendaId == 1L && it.agendaId == 0L)) && it.dayOfWeek == selectedDay
        }.sortedWith(compareBy({ it.startHour }, { it.startMinute }))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Seletor de Agendas (Múltiplas Agendas)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Agendas de Horários",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Crie múltiplas agendas para diferentes rotinas",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    FilledIconButton(
                        onClick = onOpenAddAgenda,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = PomodoroRed,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("add_agenda_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Criar nova agenda", modifier = Modifier.size(20.dp))
                    }
                }

                // Chips de seleção de agendas
                if (agendas.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        agendas.forEach { agenda ->
                            val isSelected = agenda.id == targetAgendaId
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectAgenda(agenda.id) },
                                label = {
                                    Text(
                                        agenda.name,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PomodoroRed,
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceDark,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) PomodoroRed else SurfaceCardBorder,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                modifier = Modifier.testTag("agenda_chip_${agenda.id}")
                            )
                        }
                    }
                }
            }
        }

        // Card da Agenda Ativa (com opção de editar e excluir)
        if (activeAgenda != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeAgenda.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            if (activeAgenda.description.isNotBlank()) {
                                Text(
                                    text = activeAgenda.description,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onEditAgenda(activeAgenda) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar nome da agenda",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (agendas.size > 1) {
                                IconButton(
                                    onClick = { onDeleteAgenda(activeAgenda.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir agenda",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Título do dia selecionado + Botão "+" simples para adicionar horário
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Horários — ${dayNames[selectedDay - 1]}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Blocos de estudos programados para este dia",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                FilledIconButton(
                    onClick = { onOpenAddSchedule(selectedDay) },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = PomodoroRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("add_schedule_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar novo horário", modifier = Modifier.size(20.dp))
                }
            }
        }

        // Seletor de Dia da Semana (Chips Seg-Dom)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayNames.forEachIndexed { index, name ->
                    val dayNum = index + 1
                    val isSelected = selectedDay == dayNum
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDay = dayNum },
                        label = { Text(name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PomodoroRed,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) PomodoroRed else SurfaceCardBorder,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }
        }

        if (daySchedules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Nenhum horário agendado para este dia.", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(daySchedules, key = { it.id }) { schedule ->
                ScheduleItemCard(
                    schedule = schedule,
                    onEdit = { onEditSchedule(schedule) },
                    onDelete = { onDeleteSchedule(schedule) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScheduleItemCard(
    schedule: ScheduleWithSubject,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val subjectColor = try {
        Color(android.graphics.Color.parseColor(schedule.subjectColorHex))
    } catch (_: Exception) {
        PomodoroRed
    }

    val startTime = String.format("%02d:%02d", schedule.startHour, schedule.startMinute)
    val endTime = String.format("%02d:%02d", schedule.endHour, schedule.endMinute)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${schedule.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = startTime, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "até", fontSize = 10.sp, color = TextMuted)
                Text(text = endTime, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(subjectColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = schedule.subjectName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (schedule.topic.isNotBlank()) {
                    Text(text = "Tópico: ${schedule.topic}", fontSize = 12.sp, color = TextSecondary)
                }
                if (schedule.label.isNotBlank()) {
                    Text(text = schedule.label, fontSize = 11.sp, color = TextMuted)
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = TextMuted, modifier = Modifier.size(16.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// =========================================================================
// Diálogos de Suporte (CRUD Disciplina, Tópico, Ciclo, Agenda)
// =========================================================================

@Composable
private fun AddEditSubjectDialog(
    isEditing: Boolean,
    name: String,
    selectedColorHex: String,
    availableColors: List<String>,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onColorSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Disciplina" else "Nova Disciplina",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nome da disciplina") },
                    placeholder = { Text("Ex: Física Quântica") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth().testTag("subject_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = errorMessage, color = PomodoroRed, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Cor da Disciplina", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    availableColors.forEach { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
                        val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { onColorSelect(colorHex) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed),
                modifier = Modifier.testTag("save_subject_confirm_btn")
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun EditTopicDialog(
    name: String,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Editar Assunto", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nome do assunto") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth().testTag("edit_topic_name_input"),
                    shape = RoundedCornerShape(10.dp)
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = errorMessage, color = PomodoroRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed)) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AddEditCycleDialog(
    isEditing: Boolean,
    name: String,
    description: String,
    draftItems: List<Pair<Long, Int>>,
    availableSubjects: List<SubjectEntity>,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAddSubject: (Long, Int) -> Unit,
    onRemoveSubject: (Long) -> Unit,
    onUpdateDuration: (Long, Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Ciclo de Estudo" else "Novo Ciclo de Estudo",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(380.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = { Text("Nome do ciclo") },
                        placeholder = { Text("Ex: Ciclo ENEM 2026") },
                        singleLine = true,
                        isError = errorMessage != null,
                        modifier = Modifier.fillMaxWidth().testTag("cycle_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChange,
                        label = { Text("Descrição (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = errorMessage, color = PomodoroRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Matérias no Ciclo (${draftItems.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "Digite a duração de estudo de cada matéria (em minutos):", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Matérias já no ciclo com ajuste de duração digitando
                items(draftItems) { pair ->
                    val subject = availableSubjects.firstOrNull { it.id == pair.first }
                    if (subject != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val subjectColor = try {
                                    Color(android.graphics.Color.parseColor(subject.colorHex))
                                } catch (_: Exception) {
                                    PomodoroRed
                                }

                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(subjectColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subject.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )

                                // Digitação direta do tempo em minutos
                                var minutesInput by remember(pair.first) { mutableStateOf(pair.second.toString()) }
                                OutlinedTextField(
                                    value = minutesInput,
                                    onValueChange = { input ->
                                        val digits = input.filter { it.isDigit() }
                                        minutesInput = digits
                                        val mins = digits.toIntOrNull() ?: 0
                                        if (mins > 0) {
                                            onUpdateDuration(subject.id, mins)
                                        }
                                    },
                                    label = { Text("Duração", fontSize = 11.sp) },
                                    placeholder = { Text("Ex: 50", fontSize = 11.sp) },
                                    suffix = { Text("min", fontSize = 11.sp, color = TextSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(120.dp)
                                        .testTag("cycle_subject_duration_input_${subject.id}"),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { onRemoveSubject(subject.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remover disciplina",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "+ Adicionar Disciplina ao Ciclo:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    val unselected = availableSubjects.filter { sub -> draftItems.none { it.first == sub.id } }
                    if (unselected.isEmpty()) {
                        Text(text = "Todas as disciplinas já estão adicionadas.", fontSize = 11.sp, color = TextMuted)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            unselected.forEach { sub ->
                                OutlinedButton(
                                    onClick = { onAddSubject(sub.id, 60) },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("+ ${sub.name}", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed)) {
                Text("Salvar Ciclo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AddEditScheduleDialog(
    isEditing: Boolean,
    dayOfWeek: Int,
    startHour: Int,
    startMinute: Int,
    endHour: Int,
    endMinute: Int,
    subjectId: Long?,
    topic: String,
    label: String,
    availableSubjects: List<SubjectEntity>,
    errorMessage: String?,
    onUpdate: (Int, Int, Int, Int, Int, Long?, String, String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val dayNames = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado", "Domingo")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Horário" else "Novo Horário de Estudo",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                item {
                    Text(text = "Dia da Semana", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        dayNames.forEachIndexed { index, name ->
                            val dayNum = index + 1
                            val isSelected = dayOfWeek == dayNum
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onUpdate(dayNum, startHour, startMinute, endHour, endMinute, subjectId, topic, label)
                                },
                                label = { Text(name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PomodoroRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Disciplina", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableSubjects.forEach { sub ->
                            val isSelected = subjectId == sub.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onUpdate(dayOfWeek, startHour, startMinute, endHour, endMinute, sub.id, topic, label)
                                },
                                label = { Text(sub.name, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PomodoroRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Horário de Início e Término", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = String.format("%02d:%02d", startHour, startMinute),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Início") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = String.format("%02d:%02d", endHour, endMinute),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Término") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Ajustar Hora de Início: ${startHour}h", fontSize = 12.sp, color = TextMuted)
                    Slider(
                        value = startHour.toFloat(),
                        onValueChange = {
                            val newStart = it.toInt()
                            val newEnd = if (endHour <= newStart) (newStart + 1).coerceAtMost(23) else endHour
                            onUpdate(dayOfWeek, newStart, startMinute, newEnd, endMinute, subjectId, topic, label)
                        },
                        valueRange = 0f..23f,
                        steps = 22
                    )

                    Text(text = "Ajustar Hora de Término: ${endHour}h", fontSize = 12.sp, color = TextMuted)
                    Slider(
                        value = endHour.toFloat(),
                        onValueChange = {
                            onUpdate(dayOfWeek, startHour, startMinute, it.toInt().coerceAtLeast(startHour + 1), endMinute, subjectId, topic, label)
                        },
                        valueRange = 1f..23f,
                        steps = 21
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = topic,
                        onValueChange = {
                            onUpdate(dayOfWeek, startHour, startMinute, endHour, endMinute, subjectId, it, label)
                        },
                        label = { Text("Assunto / Conteúdo (opcional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = label,
                        onValueChange = {
                            onUpdate(dayOfWeek, startHour, startMinute, endHour, endMinute, subjectId, topic, it)
                        },
                        label = { Text("Rótulo do Bloco (ex: Teoria, Revisão)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = errorMessage, color = PomodoroRed, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed)) {
                Text("Salvar Horário")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AddEditAgendaDialog(
    isEditing: Boolean,
    name: String,
    description: String,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditing) "Editar Agenda" else "Nova Agenda de Horários",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nome da Agenda") },
                    placeholder = { Text("Ex: Semestre 1, Noturno, Concurso") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Descrição (opcional)") },
                    placeholder = { Text("Ex: Grade semanal para o período letivo") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage != null) {
                    Text(text = errorMessage, color = PomodoroRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed)) {
                Text(if (isEditing) "Salvar" else "Criar Agenda")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}
