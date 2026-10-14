package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StudyCycleEntity
import com.example.data.local.entity.StudyCycleItemEntity
import kotlinx.coroutines.flow.Flow

data class CycleItemWithSubject(
    val id: Long,
    val cycleId: Long,
    val subjectId: Long,
    val targetMinutes: Int,
    val orderIndex: Int,
    val subjectName: String,
    val subjectColorHex: String
)

@Dao
interface StudyCycleDao {
    @Query("SELECT * FROM study_cycles ORDER BY created_at_epoch_ms DESC")
    fun getAllCycles(): Flow<List<StudyCycleEntity>>

    @Query("SELECT * FROM study_cycles WHERE id = :id LIMIT 1")
    suspend fun getCycleById(id: Long): StudyCycleEntity?

    @Query("""
        SELECT 
            items.id AS id,
            items.cycle_id AS cycleId,
            items.subject_id AS subjectId,
            items.target_minutes AS targetMinutes,
            items.order_index AS orderIndex,
            subjects.name AS subjectName,
            subjects.colorHex AS subjectColorHex
        FROM study_cycle_items AS items
        INNER JOIN subjects ON subjects.id = items.subject_id
        WHERE items.cycle_id = :cycleId
        ORDER BY items.order_index ASC
    """)
    fun getItemsForCycle(cycleId: Long): Flow<List<CycleItemWithSubject>>

    @Query("""
        SELECT 
            items.id AS id,
            items.cycle_id AS cycleId,
            items.subject_id AS subjectId,
            items.target_minutes AS targetMinutes,
            items.order_index AS orderIndex,
            subjects.name AS subjectName,
            subjects.colorHex AS subjectColorHex
        FROM study_cycle_items AS items
        INNER JOIN subjects ON subjects.id = items.subject_id
        ORDER BY items.cycle_id, items.order_index ASC
    """)
    fun getAllCycleItemsWithSubject(): Flow<List<CycleItemWithSubject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: StudyCycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: StudyCycleEntity)

    @Delete
    suspend fun deleteCycle(cycle: StudyCycleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycleItems(items: List<StudyCycleItemEntity>)

    @Query("DELETE FROM study_cycle_items WHERE cycle_id = :cycleId")
    suspend fun deleteItemsForCycle(cycleId: Long)
}
