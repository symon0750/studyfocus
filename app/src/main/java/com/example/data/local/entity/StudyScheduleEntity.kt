package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_schedules",
    indices = [
        Index(value = ["day_of_week"]),
        Index(value = ["subject_id"]),
        Index(value = ["agenda_id"])
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
data class StudyScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "agenda_id", defaultValue = "1")
    val agendaId: Long = 1L,

    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int, // 1 = Seg, 2 = Ter, 3 = Qua, 4 = Qui, 5 = Sex, 6 = Sáb, 7 = Dom

    @ColumnInfo(name = "start_hour")
    val startHour: Int,

    @ColumnInfo(name = "start_minute")
    val startMinute: Int,

    @ColumnInfo(name = "end_hour")
    val endHour: Int,

    @ColumnInfo(name = "end_minute")
    val endMinute: Int,

    @ColumnInfo(name = "subject_id")
    val subjectId: Long,

    @ColumnInfo(name = "topic")
    val topic: String = "",

    @ColumnInfo(name = "label")
    val label: String = ""
)
