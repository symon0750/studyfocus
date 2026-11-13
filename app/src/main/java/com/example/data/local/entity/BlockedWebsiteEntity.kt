package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_websites")
data class BlockedWebsiteEntity(
    @PrimaryKey
    val domain: String,
    val isBlocked: Boolean = true,
    val category: String = "Redes Sociais",
    val addedAtEpochMs: Long = System.currentTimeMillis()
)
