package com.example.workouttracker.data.dao

import androidx.room.*
import com.example.workouttracker.data.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise ORDER BY date DESC")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Query("DELETE FROM exercise WHERE date = :date")
    suspend fun deleteExercisesByDate(date: String)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Update
    suspend fun updateExercises(exercises: List<Exercise>)

    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Int): Exercise?

    @Query("SELECT * FROM exercise WHERE date = :date ORDER BY `order` ASC")
    suspend fun getExercisesByDate(date: String): List<Exercise>
}