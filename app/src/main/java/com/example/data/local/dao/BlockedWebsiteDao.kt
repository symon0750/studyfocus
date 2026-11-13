package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BlockedWebsiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedWebsiteDao {
    @Query("SELECT * FROM blocked_websites ORDER BY domain ASC")
    fun getAllWebsites(): Flow<List<BlockedWebsiteEntity>>

    @Query("SELECT * FROM blocked_websites WHERE isBlocked = 1")
    fun getActiveBlockedWebsites(): Flow<List<BlockedWebsiteEntity>>

    @Query("SELECT * FROM blocked_websites WHERE isBlocked = 1")
    suspend fun getActiveBlockedWebsitesList(): List<BlockedWebsiteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(website: BlockedWebsiteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(websites: List<BlockedWebsiteEntity>)

    @Update
    suspend fun update(website: BlockedWebsiteEntity)

    @Delete
    suspend fun delete(website: BlockedWebsiteEntity)

    @Query("DELETE FROM blocked_websites WHERE domain = :domain")
    suspend fun deleteByDomain(domain: String)

    @Query("UPDATE blocked_websites SET isBlocked = :blocked WHERE domain = :domain")
    suspend fun setBlockedStatus(domain: String, blocked: Boolean)
}
