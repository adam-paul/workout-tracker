package dev.elgielabs.workoutlog.data.dao

import androidx.room.*
import dev.elgielabs.workoutlog.data.model.ExerciseWithSets
import dev.elgielabs.workoutlog.data.model.Exercise
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Transaction
    @Query("SELECT * FROM exercise ORDER BY date DESC")
    fun getAllExercises(): Flow<List<ExerciseWithSets>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise): Long

    @Query("DELETE FROM exercise WHERE date = :date")
    suspend fun deleteExercisesByDate(date: String)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Update
    suspend fun updateExercises(exercises: List<Exercise>)

    @Transaction
    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    suspend fun getExerciseById(id: Int): ExerciseWithSets?

    @Transaction
    @Query("SELECT * FROM exercise WHERE id = :id")
    fun getExerciseByIdFlow(id: Int): Flow<ExerciseWithSets?>

    @Transaction
    @Query("SELECT * FROM exercise WHERE date = :date ORDER BY `order` ASC")
    suspend fun getExercisesByDate(date: String): List<ExerciseWithSets>

    @Transaction
    @Query("SELECT * FROM exercise ORDER BY date DESC")
    fun getExercisesWithSets(): Flow<List<ExerciseWithSets>>

    @Transaction
    @Query("SELECT * FROM exercise WHERE date = :date ORDER BY `order` ASC")
    fun getExercisesWithSetsByDate(date: String): Flow<List<ExerciseWithSets>>
}