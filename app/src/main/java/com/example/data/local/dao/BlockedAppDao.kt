package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BlockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedAppDao {
    @Query("SELECT * FROM blocked_apps ORDER BY appName ASC")
    fun getAllApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    fun getActiveBlockedApps(): Flow<List<BlockedAppEntity>>

    @Query("SELECT * FROM blocked_apps WHERE isBlocked = 1")
    suspend fun getActiveBlockedAppsList(): List<BlockedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: BlockedAppEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(apps: List<BlockedAppEntity>)

    @Update
    suspend fun update(app: BlockedAppEntity)

    @Delete
    suspend fun delete(app: BlockedAppEntity)

    @Query("DELETE FROM blocked_apps WHERE packageName = :packageName")
    suspend fun deleteByPackage(packageName: String)

    @Query("UPDATE blocked_apps SET isBlocked = :blocked WHERE packageName = :packageName")
    suspend fun setBlockedStatus(packageName: String, blocked: Boolean)
}
