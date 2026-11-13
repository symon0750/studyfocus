package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_cycle_items",
    indices = [
        Index(value = ["cycle_id"]),
        Index(value = ["subject_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = StudyCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycle_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StudyCycleItemEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "cycle_id")
    val cycleId: Long,

    @ColumnInfo(name = "subject_id")
    val subjectId: Long,

    @ColumnInfo(name = "target_minutes")
    val targetMinutes: Int = 60,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0
)
