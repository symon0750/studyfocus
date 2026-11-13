package com.example.data.repository

import com.example.data.local.dao.BlockSettingsDao
import com.example.data.local.dao.BlockedAppDao
import com.example.data.local.dao.BlockedWebsiteDao
import com.example.data.local.dao.CycleItemWithSubject
import com.example.data.local.dao.DailySummaryDto
import com.example.data.local.dao.ScheduleWithSubject
import com.example.data.local.dao.SessionWithSubject
import com.example.data.local.dao.StudyCycleDao
import com.example.data.local.dao.StudyScheduleDao
import com.example.data.local.dao.StudySessionDao
import com.example.data.local.dao.SubjectDao
import com.example.data.local.dao.SubjectStatDto
import com.example.data.local.dao.TopicDao
import com.example.data.local.entity.BlockSettingsEntity
import com.example.data.local.entity.BlockedAppEntity
import com.example.data.local.entity.BlockedWebsiteEntity
import com.example.data.local.entity.StudyAgendaEntity
import com.example.data.local.entity.StudyCycleEntity
import com.example.data.local.entity.StudyCycleItemEntity
import com.example.data.local.entity.StudyScheduleEntity
import com.example.data.local.entity.StudySession
import com.example.data.local.entity.SubjectEntity
import com.example.data.local.entity.TopicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class StudyRepository(
    private val subjectDao: SubjectDao,
    private val studySessionDao: StudySessionDao,
    private val topicDao: TopicDao,
    private val blockedAppDao: BlockedAppDao? = null,
    private val blockedWebsiteDao: BlockedWebsiteDao? = null,
    private val blockSettingsDao: BlockSettingsDao? = null,
    private val studyCycleDao: StudyCycleDao? = null,
    private val studyScheduleDao: StudyScheduleDao? = null
) {
    // Disciplinas
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()

    suspend fun getSubjectById(id: Long): SubjectEntity? = withContext(Dispatchers.IO) {
        subjectDao.getSubjectById(id)
    }

    suspend fun createSubject(name: String, colorHex: String, iconName: String = "book"): Long =
        withContext(Dispatchers.IO) {
            subjectDao.insertSubject(
                SubjectEntity(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }

    suspend fun updateSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        subjectDao.deleteSubject(subject)
    }

    // Assuntos / Tópicos (Vínculo com Matéria)
    val allTopics: Flow<List<TopicEntity>> = topicDao.getAllTopics()

    fun getTopicsForSubject(subjectId: Long): Flow<List<TopicEntity>> =
        topicDao.getTopicsBySubject(subjectId)

    suspend fun findTopicByName(name: String): TopicEntity? = withContext(Dispatchers.IO) {
        topicDao.findByName(name.trim())
    }

    suspend fun createTopic(name: String, subjectId: Long): Long = withContext(Dispatchers.IO) {
        topicDao.insertTopic(TopicEntity(name = name.trim(), subjectId = subjectId))
    }

    suspend fun updateTopic(topic: TopicEntity) = withContext(Dispatchers.IO) {
        topicDao.updateTopic(topic)
    }

    suspend fun deleteTopic(topic: TopicEntity) = withContext(Dispatchers.IO) {
        topicDao.deleteTopic(topic)
    }

    suspend fun deleteTopicById(id: Long) = withContext(Dispatchers.IO) {
        topicDao.deleteTopicById(id)
    }

    suspend fun getSubjectForTopic(topicName: String): SubjectEntity? = withContext(Dispatchers.IO) {
        val topic = topicDao.findByName(topicName.trim())
        if (topic != null) {
            subjectDao.getSubjectById(topic.subjectId)
        } else {
            null
        }
    }

    suspend fun associateTopicWithSubject(topicName: String, subjectId: Long) = withContext(Dispatchers.IO) {
        val trimmed = topicName.trim()
        if (trimmed.isEmpty()) return@withContext
        val existing = topicDao.findByName(trimmed)
        if (existing == null) {
            topicDao.insertTopic(TopicEntity(name = trimmed, subjectId = subjectId))
        } else {
            topicDao.updateLastUsed(existing.id)
        }
    }

    // Sessões de Estudo
    val allSessionsWithSubject: Flow<List<SessionWithSubject>> =
        studySessionDao.getAllSessionsWithSubject()

    fun getSessionsForSubject(subjectId: Long): Flow<List<SessionWithSubject>> =
        studySessionDao.getSessionsForSubject(subjectId)

    fun getRecentSessions(limit: Int = 100): Flow<List<SessionWithSubject>> =
        studySessionDao.getRecentSessionsWithSubjectPaged(limit, 0)

    fun getAllTimeStats(): Flow<DailySummaryDto> =
        studySessionDao.getAllTimeStats()

    suspend fun recordSession(
        subjectId: Long?,
        topic: String,
        studyType: String,
        phaseType: String,
        targetDurationSeconds: Int,
        actualDurationSeconds: Int,
        startTimeEpochMs: Long,
        endTimeEpochMs: Long,
        completed: Boolean,
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val finalSubjectId = if (subjectId != null) {
            subjectId
        } else {
            val matched = getSubjectForTopic(topic)
            matched?.id
        }

        if (finalSubjectId != null && topic.isNotBlank()) {
            val existingTopic = topicDao.findByName(topic.trim())
            if (existingTopic != null) {
                topicDao.updateLastUsed(existingTopic.id)
            } else {
                topicDao.insertTopic(TopicEntity(name = topic.trim(), subjectId = finalSubjectId))
            }
        }

        val session = StudySession(
            subjectId = finalSubjectId,
            topic = topic.trim().ifEmpty { "Estudo Geral" },
            studyType = studyType,
            category = phaseType,
            totalDuration = actualDurationSeconds.toLong(),
            startTime = startTimeEpochMs,
            endTime = endTimeEpochMs,
            completed = completed,
            notes = notes.trim()
        )
        studySessionDao.insertSession(session)
    }

    suspend fun recordManualSession(
        subjectId: Long,
        topic: String,
        studyType: String,
        durationMinutes: Int,
        startTimeEpochMs: Long,
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val durationSec = durationMinutes * 60
        val session = StudySession(
            subjectId = subjectId,
            topic = topic.trim().ifEmpty { "Estudo Geral" },
            studyType = studyType,
            category = "FOCUS",
            totalDuration = durationSec.toLong(),
            startTime = startTimeEpochMs,
            endTime = startTimeEpochMs + (durationSec * 1000L),
            completed = true,
            notes = notes.trim()
        )
        studySessionDao.insertSession(session)
    }

    suspend fun updateSessionNotes(sessionId: Long, notes: String) = withContext(Dispatchers.IO) {
        studySessionDao.updateNotes(sessionId, notes.trim())
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        studySessionDao.deleteSessionById(sessionId)
    }

    // Métricas e Resumos
    fun getTodaySummary(): Flow<DailySummaryDto> {
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfToday = LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return studySessionDao.getDailySummary(startOfToday, endOfToday)
    }

    fun getSubjectStats(daysLookback: Int = 30): Flow<List<SubjectStatDto>> {
        val zone = ZoneId.systemDefault()
        val startOfPeriod = LocalDate.now().minusDays(daysLookback.toLong())
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val nowEpoch = System.currentTimeMillis() + 86400000L
        return studySessionDao.getSubjectStats(startOfPeriod, nowEpoch)
    }

    fun getCurrentStreakDays(): Flow<Int> {
        return studySessionDao.getDistinctStudyDays().map { studyDays ->
            calculateStreak(studyDays)
        }
    }

    // ==========================================
    // REVISÕES ESPAÇADAS (Calculadas por Assunto)
    // ==========================================
    // Regra do Usuário:
    // 1. Agendadas automaticamente quando 2 pomodoros completos de um assunto forem concluídos.
    // 2. Não há atrasos: a próxima revisão não tem data fixa rígida, é sempre calculada a partir da última sessão realizada.
    val scheduledReviews: Flow<List<TopicReviewItem>> =
        combine(allTopics, allSubjects, allSessionsWithSubject) { topics, subjects, sessions ->
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now()
            val nowMs = System.currentTimeMillis()

            val subjectsMap = subjects.associateBy { it.id }

            topics.mapNotNull { topic ->
                val subject = subjectsMap[topic.subjectId]
                val topicSessions = sessions.filter {
                    it.topic.equals(topic.name, ignoreCase = true) ||
                    (it.subjectId != null && it.subjectId == topic.subjectId && it.topic.equals(topic.name, ignoreCase = true))
                }

                val reviewSessions = topicSessions
                    .filter { it.studyType.equals("REVIEW", ignoreCase = true) }
                    .sortedByDescending { it.startTimeEpochMs }

                val completedStudyPomodoros = topicSessions
                    .filter { it.completed && !it.studyType.equals("REVIEW", ignoreCase = true) }
                    .sortedByDescending { it.startTimeEpochMs }

                val completedPomodorosCount = completedStudyPomodoros.size
                val reviewCount = reviewSessions.size

                // Agendada AUTOMATICAMENTE quando 2 pomodoros completos de um assunto forem concluídos,
                // ou se o assunto já possui sessões de revisão realizadas.
                if (completedPomodorosCount < 2 && reviewCount == 0) {
                    return@mapNotNull null
                }

                val lastReviewDateEpochMs = reviewSessions.firstOrNull()?.let {
                    if (it.endTimeEpochMs > 0) it.endTimeEpochMs else it.startTimeEpochMs
                }

                val lastStudyDateEpochMs = completedStudyPomodoros.firstOrNull()?.let {
                    if (it.endTimeEpochMs > 0) it.endTimeEpochMs else it.startTimeEpochMs
                }

                // Cálculo da próxima revisão baseado na ÚLTIMA SESSÃO DE REVISÃO do assunto:
                val intervalDays: Int
                val baseEpochMs: Long

                if (lastReviewDateEpochMs != null) {
                    // Já houve ao menos uma sessão de revisão deste assunto: calcula a partir da última sessão de revisão
                    baseEpochMs = lastReviewDateEpochMs
                    intervalDays = when (reviewCount) {
                        1 -> 7   // Após a 1ª revisão, próxima em 7 dias a partir dela
                        2 -> 15  // Após a 2ª revisão, próxima em 15 dias a partir dela
                        3 -> 30  // Após a 3ª revisão, próxima em 30 dias a partir dela
                        else -> 30 // Revisões periódicas a cada 30 dias
                    }
                } else if (lastStudyDateEpochMs != null) {
                    // Completou os 2 pomodoros completos: 1ª revisão em 24h a contar do término do estudo
                    baseEpochMs = lastStudyDateEpochMs
                    intervalDays = 1
                } else {
                    baseEpochMs = topic.lastUsedEpochMs
                    intervalDays = 1
                }

                val nextReviewEpochMs = baseEpochMs + (intervalDays * 24L * 60 * 60 * 1000L)
                val nextDate = Instant.ofEpochMilli(nextReviewEpochMs).atZone(zone).toLocalDate()
                val daysDiff = ChronoUnit.DAYS.between(today, nextDate)

                // Não é possível atrasar revisões: não existe status OVERDUE!
                // A próxima revisão não tem data fixa; quando o usuário revisar, a seguinte será agendada a partir daquela data.
                val status = if (nowMs >= nextReviewEpochMs || daysDiff <= 0L) {
                    ReviewStatus.READY
                } else {
                    ReviewStatus.UPCOMING
                }

                TopicReviewItem(
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
            }.sortedWith(
                compareBy<TopicReviewItem> {
                    when (it.status) {
                        ReviewStatus.READY -> 0
                        ReviewStatus.UPCOMING -> 1
                    }
                }.thenBy { it.nextReviewEpochMs }
            )
        }

    // Assuntos que têm 1 pomodoro concluído e aguardam o 2º para agendamento automático
    val topicsAwaitingPomodoros: Flow<List<TopicPendingPomodoroItem>> =
        combine(allTopics, allSubjects, allSessionsWithSubject) { topics, subjects, sessions ->
            val subjectsMap = subjects.associateBy { it.id }

            topics.mapNotNull { topic ->
                val topicSessions = sessions.filter {
                    it.topic.equals(topic.name, ignoreCase = true) ||
                    (it.subjectId != null && it.subjectId == topic.subjectId && it.topic.equals(topic.name, ignoreCase = true))
                }

                val reviewSessions = topicSessions.filter { it.studyType.equals("REVIEW", ignoreCase = true) }
                val completedStudyPomodoros = topicSessions.filter { it.completed && !it.studyType.equals("REVIEW", ignoreCase = true) }

                if (completedStudyPomodoros.size == 1 && reviewSessions.isEmpty()) {
                    val subject = subjectsMap[topic.subjectId]
                    TopicPendingPomodoroItem(
                        topicId = topic.id,
                        topicName = topic.name,
                        subjectId = topic.subjectId,
                        subjectName = subject?.name ?: "Sem Matéria",
                        subjectColorHex = subject?.colorHex ?: "#EC4899",
                        completedPomodoros = completedStudyPomodoros.size
                    )
                } else {
                    null
                }
            }
        }

    suspend fun completeReview(
        subjectId: Long?,
        topicName: String,
        durationMinutes: Int = 15,
        notes: String = "Revisão periódica de assunto concluída"
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        recordSession(
            subjectId = subjectId,
            topic = topicName,
            studyType = "REVIEW",
            phaseType = "FOCUS",
            targetDurationSeconds = durationMinutes * 60,
            actualDurationSeconds = durationMinutes * 60,
            startTimeEpochMs = now - (durationMinutes * 60 * 1000L),
            endTimeEpochMs = now,
            completed = true,
            notes = notes
        )
    }

    suspend fun createTopicReview(
        subjectId: Long,
        topicName: String,
        hasInitialStudy: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        val trimmed = topicName.trim()
        val now = System.currentTimeMillis()
        val existing = topicDao.findByName(trimmed)
        val topicId = if (existing != null) {
            topicDao.updateLastUsed(existing.id)
            existing.id
        } else {
            topicDao.insertTopic(TopicEntity(name = trimmed, subjectId = subjectId, lastUsedEpochMs = now))
        }

        if (hasInitialStudy) {
            // Insere 2 pomodoros completos de estudo para atender à regra dos 2 pomodoros necessários
            recordSession(
                subjectId = subjectId,
                topic = trimmed,
                studyType = "THEORY",
                phaseType = "FOCUS",
                targetDurationSeconds = 1500,
                actualDurationSeconds = 1500,
                startTimeEpochMs = now - 3600000L,
                endTimeEpochMs = now - 1800000L,
                completed = true,
                notes = "1º Pomodoro completo de estudo"
            )
            recordSession(
                subjectId = subjectId,
                topic = trimmed,
                studyType = "EXERCISES",
                phaseType = "FOCUS",
                targetDurationSeconds = 1500,
                actualDurationSeconds = 1500,
                startTimeEpochMs = now - 1800000L,
                endTimeEpochMs = now,
                completed = true,
                notes = "2º Pomodoro completo de estudo (ativa agendamento automático de revisão)"
            )
        }
        topicId
    }

    // Ciclos de Estudo
    val allCycles: Flow<List<StudyCycleEntity>> = studyCycleDao?.getAllCycles() ?: flowOf(emptyList())
    val allCycleItems: Flow<List<CycleItemWithSubject>> = studyCycleDao?.getAllCycleItemsWithSubject() ?: flowOf(emptyList())

    fun getItemsForCycle(cycleId: Long): Flow<List<CycleItemWithSubject>> =
        studyCycleDao?.getItemsForCycle(cycleId) ?: flowOf(emptyList())

    suspend fun createStudyCycle(
        name: String,
        description: String,
        items: List<Pair<Long, Int>> // subjectId to targetMinutes
    ): Long = withContext(Dispatchers.IO) {
        val cycleDao = studyCycleDao ?: return@withContext 0L
        val cycleId = cycleDao.insertCycle(
            StudyCycleEntity(
                name = name.trim(),
                description = description.trim()
            )
        )
        val cycleItems = items.mapIndexed { index, pair ->
            StudyCycleItemEntity(
                cycleId = cycleId,
                subjectId = pair.first,
                targetMinutes = pair.second,
                orderIndex = index
            )
        }
        cycleDao.insertCycleItems(cycleItems)
        cycleId
    }

    suspend fun updateStudyCycle(
        cycle: StudyCycleEntity,
        items: List<Pair<Long, Int>>
    ) = withContext(Dispatchers.IO) {
        val cycleDao = studyCycleDao ?: return@withContext
        cycleDao.updateCycle(cycle)
        cycleDao.deleteItemsForCycle(cycle.id)
        val cycleItems = items.mapIndexed { index, pair ->
            StudyCycleItemEntity(
                cycleId = cycle.id,
                subjectId = pair.first,
                targetMinutes = pair.second,
                orderIndex = index
            )
        }
        cycleDao.insertCycleItems(cycleItems)
    }

    suspend fun deleteStudyCycle(cycle: StudyCycleEntity) = withContext(Dispatchers.IO) {
        studyCycleDao?.deleteCycle(cycle)
    }

    // Agendas de Horários
    val allAgendas: Flow<List<StudyAgendaEntity>> =
        studyScheduleDao?.getAllAgendas() ?: flowOf(emptyList())

    val allSchedules: Flow<List<ScheduleWithSubject>> =
        studyScheduleDao?.getAllSchedulesWithSubject() ?: flowOf(emptyList())

    fun getSchedulesByAgenda(agendaId: Long): Flow<List<ScheduleWithSubject>> =
        studyScheduleDao?.getSchedulesByAgenda(agendaId) ?: flowOf(emptyList())

    fun getSchedulesByDay(dayOfWeek: Int): Flow<List<ScheduleWithSubject>> =
        studyScheduleDao?.getSchedulesByDay(dayOfWeek) ?: flowOf(emptyList())

    suspend fun createAgenda(agenda: StudyAgendaEntity): Long = withContext(Dispatchers.IO) {
        studyScheduleDao?.insertAgenda(agenda) ?: 0L
    }

    suspend fun updateAgenda(agenda: StudyAgendaEntity) = withContext(Dispatchers.IO) {
        studyScheduleDao?.updateAgenda(agenda)
    }

    suspend fun deleteAgenda(agendaId: Long) = withContext(Dispatchers.IO) {
        studyScheduleDao?.deleteSchedulesByAgendaId(agendaId)
        studyScheduleDao?.deleteAgendaById(agendaId)
    }

    suspend fun createSchedule(schedule: StudyScheduleEntity): Long = withContext(Dispatchers.IO) {
        studyScheduleDao?.insertSchedule(schedule) ?: 0L
    }

    suspend fun updateSchedule(schedule: StudyScheduleEntity) = withContext(Dispatchers.IO) {
        studyScheduleDao?.updateSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: StudyScheduleEntity) = withContext(Dispatchers.IO) {
        studyScheduleDao?.deleteSchedule(schedule)
    }

    suspend fun deleteScheduleById(id: Long) = withContext(Dispatchers.IO) {
        studyScheduleDao?.deleteScheduleById(id)
    }

    // Bloqueador de Aplicativos
    val allBlockedApps: Flow<List<BlockedAppEntity>> = blockedAppDao?.getAllApps() ?: flowOf(emptyList())
    val activeBlockedApps: Flow<List<BlockedAppEntity>> = blockedAppDao?.getActiveBlockedApps() ?: flowOf(emptyList())

    suspend fun getActiveBlockedAppsList(): List<BlockedAppEntity> = withContext(Dispatchers.IO) {
        blockedAppDao?.getActiveBlockedAppsList() ?: emptyList()
    }

    suspend fun saveBlockedApp(app: BlockedAppEntity) = withContext(Dispatchers.IO) {
        blockedAppDao?.insertOrUpdate(app)
    }

    suspend fun saveBlockedApps(apps: List<BlockedAppEntity>) = withContext(Dispatchers.IO) {
        blockedAppDao?.insertAll(apps)
    }

    suspend fun setAppBlockedStatus(packageName: String, blocked: Boolean) = withContext(Dispatchers.IO) {
        blockedAppDao?.setBlockedStatus(packageName, blocked)
    }

    suspend fun removeBlockedApp(packageName: String) = withContext(Dispatchers.IO) {
        blockedAppDao?.deleteByPackage(packageName)
    }

    // Bloqueador de Sites
    val allBlockedWebsites: Flow<List<BlockedWebsiteEntity>> = blockedWebsiteDao?.getAllWebsites() ?: flowOf(emptyList())
    val activeBlockedWebsites: Flow<List<BlockedWebsiteEntity>> = blockedWebsiteDao?.getActiveBlockedWebsites() ?: flowOf(emptyList())

    suspend fun getActiveBlockedWebsitesList(): List<BlockedWebsiteEntity> = withContext(Dispatchers.IO) {
        blockedWebsiteDao?.getActiveBlockedWebsitesList() ?: emptyList()
    }

    suspend fun saveBlockedWebsite(website: BlockedWebsiteEntity) = withContext(Dispatchers.IO) {
        blockedWebsiteDao?.insertOrUpdate(website)
    }

    suspend fun setWebsiteBlockedStatus(domain: String, blocked: Boolean) = withContext(Dispatchers.IO) {
        blockedWebsiteDao?.setBlockedStatus(domain, blocked)
    }

    suspend fun removeBlockedWebsite(domain: String) = withContext(Dispatchers.IO) {
        blockedWebsiteDao?.deleteByDomain(domain)
    }

    // Configurações do Bloqueador
    val blockSettings: Flow<BlockSettingsEntity> = blockedAppDao?.let {
        blockSettingsDao?.getSettings()?.map { it ?: BlockSettingsEntity() }
    } ?: flowOf(BlockSettingsEntity())

    suspend fun getBlockSettingsSync(): BlockSettingsEntity = withContext(Dispatchers.IO) {
        blockSettingsDao?.getSettingsSync() ?: BlockSettingsEntity()
    }

    suspend fun updateBlockSettings(settings: BlockSettingsEntity) = withContext(Dispatchers.IO) {
        blockSettingsDao?.saveSettings(settings)
    }

    companion object {
        fun calculateStreak(studyDays: List<String>): Int {
            if (studyDays.isEmpty()) return 0
            val today = LocalDate.now()
            val parsedDays = studyDays.mapNotNull {
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    null
                }
            }.distinct().sortedDescending()

            if (parsedDays.isEmpty()) return 0

            val mostRecent = parsedDays.first()
            val diffFromToday = ChronoUnit.DAYS.between(mostRecent, today)

            if (diffFromToday > 1) return 0

            var streak = 1
            for (i in 0 until parsedDays.size - 1) {
                val current = parsedDays[i]
                val prev = parsedDays[i + 1]
                if (ChronoUnit.DAYS.between(prev, current) == 1L) {
                    streak++
                } else {
                    break
                }
            }
            return streak
        }
    }
}

enum class ReviewStatus {
    READY,      // Intervalo concluído: disponível para realizar revisão (sem conceito de atraso!)
    UPCOMING    // Intervalo em andamento: aguardando próxima data calculada
}

data class TopicReviewItem(
    val topicId: Long,
    val topicName: String,
    val subjectId: Long?,
    val subjectName: String,
    val subjectColorHex: String,
    val lastReviewDateEpochMs: Long?,
    val lastStudyDateEpochMs: Long?,
    val reviewCount: Int,
    val completedPomodorosCount: Int,
    val intervalDays: Int,
    val nextReviewEpochMs: Long,
    val status: ReviewStatus,
    val daysDiff: Long
)

data class TopicPendingPomodoroItem(
    val topicId: Long,
    val topicName: String,
    val subjectId: Long?,
    val subjectName: String,
    val subjectColorHex: String,
    val completedPomodoros: Int
)


