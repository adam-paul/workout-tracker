package com.example.workouttracker.data.dao

import androidx.room.*
import com.example.workouttracker.data.model.ExerciseSet
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseSetDao {
    @Query("SELECT * FROM exercise_set WHERE exercise_id = :exerciseId ORDER BY `order` ASC")
    fun getSetsByExerciseId(exerciseId: Int): Flow<List<ExerciseSet>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: ExerciseSet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<ExerciseSet>)

    @Update
    suspend fun updateSet(set: ExerciseSet)

    @Update
    suspend fun updateSets(sets: List<ExerciseSet>)

    @Delete
    suspend fun deleteSet(set: ExerciseSet)

    @Query("DELETE FROM exercise_set WHERE exercise_id = :exerciseId")
    suspend fun deleteSetsByExerciseId(exerciseId: Int)

    @Query("DELETE FROM exercise_set WHERE id = :setId")
    suspend fun deleteSetById(setId: Int)

    @Transaction
    suspend fun updateSetsForExercise(exerciseId: Int, newSets: List<ExerciseSet>) {
        // Delete all existing sets
        deleteSetsByExerciseId(exerciseId)
        // Insert new sets
        insertSets(newSets)
    }
}
