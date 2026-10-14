package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topics",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["subject_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "subject_id")
    val subjectId: Long,

    @ColumnInfo(name = "last_used_epoch_ms")
    val lastUsedEpochMs: Long = System.currentTimeMillis()
)
