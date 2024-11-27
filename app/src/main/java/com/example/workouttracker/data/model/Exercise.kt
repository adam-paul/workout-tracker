package com.example.workouttracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "weight") val weight: String,
    @ColumnInfo(name = "reps_or_duration") val repsOrDuration: String,
    @ColumnInfo(name = "notes") val notes: String = "",
    @ColumnInfo(name = "order") val order: Int = 0
)