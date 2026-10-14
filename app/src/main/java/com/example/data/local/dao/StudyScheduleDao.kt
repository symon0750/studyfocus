package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StudyAgendaEntity
import com.example.data.local.entity.StudyScheduleEntity
import kotlinx.coroutines.flow.Flow

data class ScheduleWithSubject(
    val id: Long,
    val agendaId: Long = 1L,
    val dayOfWeek: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val subjectId: Long,
    val topic: String,
    val label: String,
    val subjectName: String,
    val subjectColorHex: String
)

@Dao
interface StudyScheduleDao {
    @Query("""
        SELECT 
            schedules.id AS id,
            schedules.agenda_id AS agendaId,
            schedules.day_of_week AS dayOfWeek,
            schedules.start_hour AS startHour,
            schedules.start_minute AS startMinute,
            schedules.end_hour AS endHour,
            schedules.end_minute AS endMinute,
            schedules.subject_id AS subjectId,
            schedules.topic AS topic,
            schedules.label AS label,
            subjects.name AS subjectName,
            subjects.colorHex AS subjectColorHex
        FROM study_schedules AS schedules
        INNER JOIN subjects ON subjects.id = schedules.subject_id
        ORDER BY schedules.day_of_week ASC, schedules.start_hour ASC, schedules.start_minute ASC
    """)
    fun getAllSchedulesWithSubject(): Flow<List<ScheduleWithSubject>>

    @Query("""
        SELECT 
            schedules.id AS id,
            schedules.agenda_id AS agendaId,
            schedules.day_of_week AS dayOfWeek,
            schedules.start_hour AS startHour,
            schedules.start_minute AS startMinute,
            schedules.end_hour AS endHour,
            schedules.end_minute AS endMinute,
            schedules.subject_id AS subjectId,
            schedules.topic AS topic,
            schedules.label AS label,
            subjects.name AS subjectName,
            subjects.colorHex AS subjectColorHex
        FROM study_schedules AS schedules
        INNER JOIN subjects ON subjects.id = schedules.subject_id
        WHERE schedules.agenda_id = :agendaId
        ORDER BY schedules.day_of_week ASC, schedules.start_hour ASC, schedules.start_minute ASC
    """)
    fun getSchedulesByAgenda(agendaId: Long): Flow<List<ScheduleWithSubject>>

    @Query("""
        SELECT 
            schedules.id AS id,
            schedules.agenda_id AS agendaId,
            schedules.day_of_week AS dayOfWeek,
            schedules.start_hour AS startHour,
            schedules.start_minute AS startMinute,
            schedules.end_hour AS endHour,
            schedules.end_minute AS endMinute,
            schedules.subject_id AS subjectId,
            schedules.topic AS topic,
            schedules.label AS label,
            subjects.name AS subjectName,
            subjects.colorHex AS subjectColorHex
        FROM study_schedules AS schedules
        INNER JOIN subjects ON subjects.id = schedules.subject_id
        WHERE schedules.day_of_week = :dayOfWeek
        ORDER BY schedules.start_hour ASC, schedules.start_minute ASC
    """)
    fun getSchedulesByDay(dayOfWeek: Int): Flow<List<ScheduleWithSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: StudyScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: StudyScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: StudyScheduleEntity)

    @Query("DELETE FROM study_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("DELETE FROM study_schedules WHERE agenda_id = :agendaId")
    suspend fun deleteSchedulesByAgendaId(agendaId: Long)

    // Agendas
    @Query("SELECT * FROM study_agendas ORDER BY created_at ASC")
    fun getAllAgendas(): Flow<List<StudyAgendaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgenda(agenda: StudyAgendaEntity): Long

    @Update
    suspend fun updateAgenda(agenda: StudyAgendaEntity)

    @Delete
    suspend fun deleteAgenda(agenda: StudyAgendaEntity)

    @Query("DELETE FROM study_agendas WHERE id = :id")
    suspend fun deleteAgendaById(id: Long)
}
