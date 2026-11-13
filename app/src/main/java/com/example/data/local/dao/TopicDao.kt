package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY last_used_epoch_ms DESC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: Long): TopicEntity?

    @Query("SELECT * FROM topics WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): TopicEntity?

    @Query("SELECT * FROM topics WHERE subject_id = :subjectId AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByNameAndSubject(name: String, subjectId: Long): TopicEntity?

    @Query("SELECT * FROM topics WHERE subject_id = :subjectId ORDER BY name ASC")
    fun getTopicsBySubject(subjectId: Long): Flow<List<TopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopicById(id: Long)

    @Query("UPDATE topics SET last_used_epoch_ms = :time WHERE id = :id")
    suspend fun updateLastUsed(id: Long, time: Long = System.currentTimeMillis())
}
