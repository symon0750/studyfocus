package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidade Room representando uma sessão de estudo no SQLite.
 *
 * Inclui campos para:
 * - id: Identificador único autoincrementado
 * - startTime: Timestamp inicial da sessão em epoch milissegundos
 * - endTime: Timestamp de finalização da sessão em epoch milissegundos
 * - category: Categoria/fase de estudo (ex: "FOCUS", "SHORT_BREAK", "LONG_BREAK")
 * - totalDuration: Duração total da sessão em segundos
 * - subjectId: Vínculo com a matéria/disciplina
 * - topic: Assunto específico estudado (ex: "Funções Trigonométricas")
 * - studyType: Tipo de estudo ("READING", "EXERCISES", "VIDEO", "REVIEW")
 * - completed: Booleano indicando se a sessão foi concluída integralmente
 * - notes: Anotações da sessão
 */
@Entity(
    tableName = "study_sessions",
    indices = [
        Index(value = ["start_time"]),
        Index(value = ["category", "start_time"]),
        Index(value = ["category", "completed"]),
        Index(value = ["subject_id", "start_time"]),
        Index(value = ["topic", "start_time"]),
        Index(value = ["study_type", "start_time"]),
        Index(value = ["completed", "start_time"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class StudySession(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long,

    @ColumnInfo(name = "category")
    val category: String = "FOCUS",

    @ColumnInfo(name = "total_duration")
    val totalDuration: Long,

    @ColumnInfo(name = "subject_id")
    val subjectId: Long? = null,

    @ColumnInfo(name = "topic")
    val topic: String = "",

    @ColumnInfo(name = "study_type")
    val studyType: String = "READING",

    @ColumnInfo(name = "completed")
    val completed: Boolean = true,

    @ColumnInfo(name = "notes")
    val notes: String = ""
) {
    val startTimeEpochMs: Long get() = startTime
    val endTimeEpochMs: Long get() = endTime
    val phaseType: String get() = category
    val actualDurationSeconds: Int get() = totalDuration.toInt()
    val targetDurationSeconds: Int get() = totalDuration.toInt()
}
