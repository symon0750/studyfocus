package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.BlockSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockSettingsDao {
    @Query("SELECT * FROM block_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<BlockSettingsEntity?>

    @Query("SELECT * FROM block_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): BlockSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: BlockSettingsEntity)

    @Update
    suspend fun updateSettings(settings: BlockSettingsEntity)
}
