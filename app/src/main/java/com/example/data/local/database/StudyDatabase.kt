package com.example.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BlockSettingsDao
import com.example.data.local.dao.BlockedAppDao
import com.example.data.local.dao.BlockedWebsiteDao
import com.example.data.local.dao.StudyCycleDao
import com.example.data.local.dao.StudyScheduleDao
import com.example.data.local.dao.StudySessionDao
import com.example.data.local.dao.SubjectDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubjectEntity::class,
        TopicEntity::class,
        StudySession::class,
        BlockedAppEntity::class,
        BlockedWebsiteEntity::class,
        BlockSettingsEntity::class,
        StudyCycleEntity::class,
        StudyCycleItemEntity::class,
        StudyScheduleEntity::class,
        StudyAgendaEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun topicDao(): TopicDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun blockedWebsiteDao(): BlockedWebsiteDao
    abstract fun blockSettingsDao(): BlockSettingsDao
    abstract fun studyCycleDao(): StudyCycleDao
    abstract fun studyScheduleDao(): StudyScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: StudyDatabase? = null

        fun getInstance(context: Context): StudyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyDatabase::class.java,
                    "study_focus.db"
                )
                    .fallbackToDestructiveMigration()
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            populateDefaultData(context)
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            populateDefaultData(context)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun populateDefaultData(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = getInstance(context)
                
                // Disciplinas padrão
                val subjectIds = db.subjectDao().insertSubjects(
                    listOf(
                        SubjectEntity(name = "Matemática", colorHex = "#3B82F6"),
                        SubjectEntity(name = "Português", colorHex = "#10B981"),
                        SubjectEntity(name = "Programação", colorHex = "#F59E0B"),
                        SubjectEntity(name = "História", colorHex = "#EC4899"),
                        SubjectEntity(name = "Ciências", colorHex = "#8B5CF6")
                    )
                )

                // Tópicos iniciais por matéria
                if (subjectIds.isNotEmpty()) {
                    val matId = subjectIds[0]
                    val portId = if (subjectIds.size > 1) subjectIds[1] else matId
                    val progId = if (subjectIds.size > 2) subjectIds[2] else matId

                    db.topicDao().insertTopic(TopicEntity(name = "Álgebra Linear", subjectId = matId))
                    db.topicDao().insertTopic(TopicEntity(name = "Cálculo I", subjectId = matId))
                    db.topicDao().insertTopic(TopicEntity(name = "Gramática & Sintaxe", subjectId = portId))
                    db.topicDao().insertTopic(TopicEntity(name = "Interpretação de Texto", subjectId = portId))
                    db.topicDao().insertTopic(TopicEntity(name = "Kotlin & Android", subjectId = progId))
                    db.topicDao().insertTopic(TopicEntity(name = "Estruturas de Dados", subjectId = progId))

                    // Ciclo de estudo de demonstração
                    val cycleId = db.studyCycleDao().insertCycle(
                        StudyCycleEntity(
                            name = "Ciclo Foco Geral",
                            description = "Ciclo equilibrado de matérias exatas, humanas e técnicas"
                        )
                    )
                    db.studyCycleDao().insertCycleItems(
                        listOf(
                            StudyCycleItemEntity(cycleId = cycleId, subjectId = matId, targetMinutes = 120, orderIndex = 0),
                            StudyCycleItemEntity(cycleId = cycleId, subjectId = portId, targetMinutes = 90, orderIndex = 1),
                            StudyCycleItemEntity(cycleId = cycleId, subjectId = progId, targetMinutes = 90, orderIndex = 2)
                        )
                    )

                    // Agenda Semanal Padrão e Grade horária semanal de demonstração
                    val defaultAgendaId = db.studyScheduleDao().insertAgenda(
                        StudyAgendaEntity(
                            id = 1L,
                            name = "Agenda Principal",
                            description = "Grade horária padrão semanal",
                            isActive = true
                        )
                    )

                    db.studyScheduleDao().insertSchedule(
                        StudyScheduleEntity(
                            agendaId = defaultAgendaId,
                            dayOfWeek = 1, // Segunda
                            startHour = 8, startMinute = 0,
                            endHour = 10, endMinute = 0,
                            subjectId = matId,
                            topic = "Álgebra Linear",
                            label = "Bloco Matinal"
                        )
                    )
                    db.studyScheduleDao().insertSchedule(
                        StudyScheduleEntity(
                            agendaId = defaultAgendaId,
                            dayOfWeek = 1, // Segunda
                            startHour = 14, startMinute = 0,
                            endHour = 16, endMinute = 0,
                            subjectId = progId,
                            topic = "Kotlin & Android",
                            label = "Bloco da Tarde"
                        )
                    )
                    db.studyScheduleDao().insertSchedule(
                        StudyScheduleEntity(
                            agendaId = defaultAgendaId,
                            dayOfWeek = 2, // Terça
                            startHour = 9, startMinute = 0,
                            endHour = 11, endMinute = 0,
                            subjectId = portId,
                            topic = "Gramática & Sintaxe",
                            label = "Português Teórico"
                        )
                    )

                    // Sessões de Estudo e Revisões periódicas de demonstração (regra: 2 pomodoros completos)
                    val now = System.currentTimeMillis()
                    val dayMs = 24L * 60 * 60 * 1000L

                    // 1. "Álgebra Linear" (2 pomodoros concluídos + 1 revisão há 7 dias) -> Próxima revisão (7 dias após a 1ª) DISPONÍVEL HOJE
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = matId,
                            topic = "Álgebra Linear",
                            studyType = "THEORY",
                            category = "FOCUS",
                            totalDuration = 1500,
                            startTime = now - (9 * dayMs),
                            endTime = now - (9 * dayMs) + 1500000L,
                            completed = true,
                            notes = "1º Pomodoro: Teoria de matrizes"
                        )
                    )
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = matId,
                            topic = "Álgebra Linear",
                            studyType = "EXERCISES",
                            category = "FOCUS",
                            totalDuration = 1500,
                            startTime = now - (8 * dayMs),
                            endTime = now - (8 * dayMs) + 1500000L,
                            completed = true,
                            notes = "2º Pomodoro: Exercícios práticos (agendamento automático de revisão ativado!)"
                        )
                    )
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = matId,
                            topic = "Álgebra Linear",
                            studyType = "REVIEW",
                            category = "FOCUS",
                            totalDuration = 1200,
                            startTime = now - (7 * dayMs) - 1200000L,
                            endTime = now - (7 * dayMs),
                            completed = true,
                            notes = "1ª Sessão de Revisão periódica realizada há 7 dias"
                        )
                    )

                    // 2. "Gramática & Sintaxe" (2 pomodoros concluídos + 1 revisão há 2 dias) -> Próxima revisão em 5 dias
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = portId,
                            topic = "Gramática & Sintaxe",
                            studyType = "THEORY",
                            category = "FOCUS",
                            totalDuration = 1500,
                            startTime = now - (4 * dayMs),
                            endTime = now - (4 * dayMs) + 1500000L,
                            completed = true,
                            notes = "1º Pomodoro: Análise sintática"
                        )
                    )
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = portId,
                            topic = "Gramática & Sintaxe",
                            studyType = "EXERCISES",
                            category = "FOCUS",
                            totalDuration = 1500,
                            startTime = now - (3 * dayMs),
                            endTime = now - (3 * dayMs) + 1500000L,
                            completed = true,
                            notes = "2º Pomodoro: Questões de concurso"
                        )
                    )
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = portId,
                            topic = "Gramática & Sintaxe",
                            studyType = "REVIEW",
                            category = "FOCUS",
                            totalDuration = 1200,
                            startTime = now - (2 * dayMs) - 1200000L,
                            endTime = now - (2 * dayMs),
                            completed = true,
                            notes = "1ª Sessão de Revisão periódica realizada há 2 dias"
                        )
                    )

                    // 3. "Kotlin & Android" (2 pomodoros concluídos há 1 dia, sem revisão ainda) -> 1ª revisão (24h) DISPONÍVEL HOJE!
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = progId,
                            topic = "Kotlin & Android",
                            studyType = "THEORY",
                            category = "FOCUS",
                            totalDuration = 1500,
                            startTime = now - (1 * dayMs) - 3000000L,
                            endTime = now - (1 * dayMs) - 1500000L,
                            completed = true,
                            notes = "1º Pomodoro: Arquitetura Jetpack Compose"
                        )
                    )
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = progId,
                            topic = "Kotlin & Android",
                            studyType = "EXERCISES",
                            category = "FOCUS",
                            totalDuration = 1500,
                            startTime = now - (1 * dayMs) - 1500000L,
                            endTime = now - (1 * dayMs),
                            completed = true,
                            notes = "2º Pomodoro concluído: ativa 1ª revisão de 24h automaticamente"
                        )
                    )

                    // 4. "Cálculo I" (1 pomodoro concluído) -> Aguarda o 2º pomodoro para agendar revisão
                    db.studySessionDao().insert(
                        StudySession(
                            subjectId = matId,
                            topic = "Cálculo I",
                            studyType = "THEORY",
                            category = "FOCUS",
                            totalDuration = 1500,
                            startTime = now - 7200000L,
                            endTime = now - 5700000L,
                            completed = true,
                            notes = "1º Pomodoro: Limites e Continuidade"
                        )
                    )
                }

                // Apps de distração conhecidos pré-cadastrados
                db.blockedAppDao().insertAll(
                    listOf(
                        BlockedAppEntity(packageName = "com.instagram.android", appName = "Instagram", isBlocked = true, category = "Redes Sociais"),
                        BlockedAppEntity(packageName = "com.zhiliaoapp.musically", appName = "TikTok", isBlocked = true, category = "Redes Sociais"),
                        BlockedAppEntity(packageName = "com.google.android.youtube", appName = "YouTube", isBlocked = true, category = "Vídeo"),
                        BlockedAppEntity(packageName = "com.twitter.android", appName = "X / Twitter", isBlocked = true, category = "Redes Sociais"),
                        BlockedAppEntity(packageName = "com.facebook.katana", appName = "Facebook", isBlocked = true, category = "Redes Sociais"),
                        BlockedAppEntity(packageName = "com.reddit.frontpage", appName = "Reddit", isBlocked = true, category = "Fóruns"),
                        BlockedAppEntity(packageName = "com.netflix.mediaclient", appName = "Netflix", isBlocked = true, category = "Streaming"),
                        BlockedAppEntity(packageName = "tv.twitch.android.app", appName = "Twitch", isBlocked = true, category = "Streaming")
                    )
                )

                // Sites de distração comuns pré-cadastrados
                db.blockedWebsiteDao().insertAll(
                    listOf(
                        BlockedWebsiteEntity(domain = "instagram.com", isBlocked = true, category = "Redes Sociais"),
                        BlockedWebsiteEntity(domain = "tiktok.com", isBlocked = true, category = "Redes Sociais"),
                        BlockedWebsiteEntity(domain = "twitter.com", isBlocked = true, category = "Redes Sociais"),
                        BlockedWebsiteEntity(domain = "x.com", isBlocked = true, category = "Redes Sociais"),
                        BlockedWebsiteEntity(domain = "facebook.com", isBlocked = true, category = "Redes Sociais"),
                        BlockedWebsiteEntity(domain = "youtube.com", isBlocked = true, category = "Vídeo"),
                        BlockedWebsiteEntity(domain = "reddit.com", isBlocked = true, category = "Fóruns"),
                        BlockedWebsiteEntity(domain = "netflix.com", isBlocked = true, category = "Streaming"),
                        BlockedWebsiteEntity(domain = "twitch.tv", isBlocked = true, category = "Streaming")
                    )
                )

                // Configurações padrão
                db.blockSettingsDao().saveSettings(BlockSettingsEntity())
            }
        }
    }
}
