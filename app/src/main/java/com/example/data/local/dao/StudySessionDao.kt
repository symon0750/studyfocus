package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.StudySession
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para a entidade [StudySession].
 *
 * Fornece métodos CRUD completos e consultas reativas de alto desempenho via [Flow]:
 * - Inserção atômica
 * - Exclusão (por entidade ou por id)
 * - Consulta de todas as sessões (Query All)
 * - Consulta filtrada por categoria (Query by Category)
 * - Consultas agregadas diretamente no SQLite para relatórios analíticos de longo prazo
 */
@Dao
interface StudySessionDao {

    // --- MÉTODOS CRUD BÁSICOS ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySession): Long = insert(session)

    @Delete
    suspend fun delete(session: StudySession)

    @Delete
    suspend fun deleteSession(session: StudySession) = delete(session)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT * FROM study_sessions ORDER BY start_time DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE category = :category ORDER BY start_time DESC")
    fun getSessionsByCategory(category: String): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): StudySession?

    // --- MÉTODOS DE HISTÓRICO PAGINADO E ALTA PERFORMANCE ---

    @Query("""
        SELECT 
            sess.id AS id,
            sess.subject_id AS subjectId,
            sub.name AS subjectName,
            sub.colorHex AS subjectColor,
            COALESCE(sess.topic, '') AS topic,
            COALESCE(sess.study_type, 'READING') AS studyType,
            sess.category AS phaseType,
            sess.total_duration AS targetDurationSeconds,
            sess.total_duration AS actualDurationSeconds,
            sess.start_time AS startTimeEpochMs,
            sess.end_time AS endTimeEpochMs,
            sess.completed AS completed,
            sess.notes AS notes
        FROM study_sessions sess
        LEFT JOIN subjects sub ON sess.subject_id = sub.id
        ORDER BY sess.start_time DESC
        LIMIT :limit OFFSET :offset
    """)
    fun getRecentSessionsWithSubjectPaged(limit: Int, offset: Int): Flow<List<SessionWithSubject>>

    @Query("""
        SELECT 
            sess.id AS id,
            sess.subject_id AS subjectId,
            sub.name AS subjectName,
            sub.colorHex AS subjectColor,
            COALESCE(sess.topic, '') AS topic,
            COALESCE(sess.study_type, 'READING') AS studyType,
            sess.category AS phaseType,
            sess.total_duration AS targetDurationSeconds,
            sess.total_duration AS actualDurationSeconds,
            sess.start_time AS startTimeEpochMs,
            sess.end_time AS endTimeEpochMs,
            sess.completed AS completed,
            sess.notes AS notes
        FROM study_sessions sess
        LEFT JOIN subjects sub ON sess.subject_id = sub.id
        ORDER BY sess.start_time DESC
    """)
    fun getAllSessionsWithSubject(): Flow<List<SessionWithSubject>>

    @Query("""
        SELECT 
            sess.id AS id,
            sess.subject_id AS subjectId,
            sub.name AS subjectName,
            sub.colorHex AS subjectColor,
            COALESCE(sess.topic, '') AS topic,
            COALESCE(sess.study_type, 'READING') AS studyType,
            sess.category AS phaseType,
            sess.total_duration AS targetDurationSeconds,
            sess.total_duration AS actualDurationSeconds,
            sess.start_time AS startTimeEpochMs,
            sess.end_time AS endTimeEpochMs,
            sess.completed AS completed,
            sess.notes AS notes
        FROM study_sessions sess
        LEFT JOIN subjects sub ON sess.subject_id = sub.id
        WHERE sess.subject_id = :subjectId
        ORDER BY sess.start_time DESC
    """)
    fun getSessionsForSubject(subjectId: Long): Flow<List<SessionWithSubject>>

    @Query("UPDATE study_sessions SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("""
        UPDATE study_sessions 
        SET topic = :newTopicName 
        WHERE LOWER(TRIM(topic)) = LOWER(TRIM(:oldTopicName))
          AND (:subjectId IS NULL OR subject_id = :subjectId)
    """)
    suspend fun updateTopicName(oldTopicName: String, newTopicName: String, subjectId: Long? = null)

    fun getDailySummary(startEpochMs: Long, endEpochMs: Long): Flow<DailySummaryDto> =
        getDailyAggregation(startEpochMs, endEpochMs)

    // --- MÉTODOS ANALÍTICOS DE AGREGAÇÃO SQL (SUB-MILISSEGUNDO MESMO COM ANOS DE DADOS) ---

    @Query("""
        SELECT 
            COALESCE(SUM(total_duration), 0) AS totalSeconds,
            COUNT(*) AS totalSessions,
            COUNT(CASE WHEN completed = 1 THEN 1 END) AS completedSessions
        FROM study_sessions
        WHERE DATE(start_time / 1000, 'unixepoch', 'localtime') = DATE('now', 'localtime')
          AND category = 'FOCUS'
    """)
    fun getTodaySummary(): Flow<DailySummaryDto>

    @Query("""
        SELECT 
            COALESCE(SUM(total_duration), 0) AS totalSeconds,
            COUNT(*) AS totalSessions,
            COUNT(CASE WHEN completed = 1 THEN 1 END) AS completedSessions
        FROM study_sessions
        WHERE start_time >= :startEpochMs AND start_time < :endEpochMs
          AND category = 'FOCUS'
    """)
    fun getDailyAggregation(startEpochMs: Long, endEpochMs: Long): Flow<DailySummaryDto>

    @Query("""
        SELECT 
            COALESCE(SUM(total_duration), 0) AS totalSeconds,
            COUNT(*) AS totalSessions,
            COUNT(CASE WHEN completed = 1 THEN 1 END) AS completedSessions
        FROM study_sessions
        WHERE category = 'FOCUS'
    """)
    fun getAllTimeStats(): Flow<DailySummaryDto>

    @Query("""
        SELECT 
            s.id AS subjectId,
            s.name AS subjectName,
            s.colorHex AS colorHex,
            COALESCE(SUM(sess.total_duration), 0) AS totalDurationSeconds,
            COUNT(sess.id) AS sessionCount
        FROM subjects s
        LEFT JOIN study_sessions sess 
            ON s.id = sess.subject_id 
            AND sess.start_time >= :startEpochMs 
            AND sess.start_time < :endEpochMs
            AND sess.category = 'FOCUS'
        GROUP BY s.id
        ORDER BY totalDurationSeconds DESC
    """)
    fun getSubjectStats(startEpochMs: Long, endEpochMs: Long): Flow<List<SubjectStatDto>>

    @Query("""
        SELECT DISTINCT DATE(start_time / 1000, 'unixepoch', 'localtime') AS study_day
        FROM study_sessions
        WHERE category = 'FOCUS' AND (completed = 1 OR total_duration > 0)
        ORDER BY study_day DESC
    """)
    fun getDistinctStudyDays(): Flow<List<String>>

    @Query("""
        SELECT 
            LOWER(TRIM(topic)) AS topicKey,
            COUNT(CASE WHEN category = 'FOCUS' AND completed = 1 AND UPPER(study_type) != 'REVIEW' THEN 1 END) AS completedPomodorosCount,
            MAX(CASE WHEN category = 'FOCUS' AND completed = 1 AND UPPER(study_type) != 'REVIEW' THEN (CASE WHEN end_time > 0 THEN end_time ELSE start_time END) END) AS lastStudyDateEpochMs,
            COUNT(CASE WHEN category = 'FOCUS' AND UPPER(study_type) = 'REVIEW' THEN 1 END) AS reviewCount,
            MAX(CASE WHEN category = 'FOCUS' AND UPPER(study_type) = 'REVIEW' THEN (CASE WHEN end_time > 0 THEN end_time ELSE start_time END) END) AS lastReviewDateEpochMs
        FROM study_sessions
        WHERE topic IS NOT NULL AND TRIM(topic) != ''
        GROUP BY LOWER(TRIM(topic))
    """)
    fun getTopicSessionSummaries(): Flow<List<TopicSessionSummaryDto>>

    @Query("SELECT COUNT(*) FROM study_sessions")
    suspend fun getCount(): Int
}
