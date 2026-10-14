package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_settings")
data class BlockSettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val isBlockingEnabled: Boolean = true,
    val blockDuringBreaks: Boolean = false,
    val strictMode: Boolean = false,
    val showWarningOverlay: Boolean = true
)
