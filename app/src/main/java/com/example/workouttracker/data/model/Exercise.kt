package com.example.workouttracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

data class ExerciseWithSets(
    @Embedded val exercise: Exercise,
    @Relation(
        parentColumn = "id",
        entityColumn = "exercise_id"
    )
    val sets: List<ExerciseSet>
)

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "order") val order: Int = 0
)

@Entity(
    tableName = "exercise_set",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exercise_id")]
)
data class ExerciseSet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "weight") val weight: String,
    @ColumnInfo(name = "reps_or_duration") val repsOrDuration: String,
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "order") val order: Int = 0
)

data class SetState(
    val weight: String,
    val repsOrDuration: String,
    val notes: String
)