package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: Long): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>): List<Long>

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getCount(): Int
}
