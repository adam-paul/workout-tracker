package dev.elgielabs.workoutlog.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.elgielabs.workoutlog.data.dao.ExerciseDao
import dev.elgielabs.workoutlog.data.dao.ExerciseSetDao
import dev.elgielabs.workoutlog.data.model.Exercise
import dev.elgielabs.workoutlog.data.model.ExerciseSet
import dev.elgielabs.workoutlog.data.model.ExerciseWithSets
import dev.elgielabs.workoutlog.data.model.SetState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class ExerciseViewModel(
    private val exerciseDao: ExerciseDao,
    private val setDao: ExerciseSetDao
) : ViewModel() {

    val exercisesByMonth: Flow<Map<YearMonth, Map<LocalDate, List<ExerciseWithSets>>>> = exerciseDao.getAllExercises()
        .map { exercises ->
            exercises.groupBy { YearMonth.from(LocalDate.parse(it.exercise.date)) }
                .mapValues { (_, monthExercises) ->
                    monthExercises.groupBy { LocalDate.parse(it.exercise.date) }
                }
        }

    private val _expandedMonths = mutableStateListOf<YearMonth>()
    val expandedMonths: List<YearMonth> get() = _expandedMonths

    init {
        viewModelScope.launch {
            exercisesByMonth.collect { exercisesByMonthMap ->
                val mostRecentMonth = exercisesByMonthMap.keys.maxOrNull()
                if (mostRecentMonth != null && !_expandedMonths.contains(mostRecentMonth)) {
                    _expandedMonths.add(mostRecentMonth)
                }
            }
        }
    }

    fun toggleMonth(month: YearMonth) {
        if (_expandedMonths.contains(month)) {
            _expandedMonths.remove(month)
        } else {
            _expandedMonths.add(month)
        }
    }

    fun updateWorkoutDate(oldDate: LocalDate, newDate: LocalDate) {
        viewModelScope.launch {
            val exercises = exerciseDao.getExercisesByDate(oldDate.toString())
            exercises.forEach { exerciseWithSets ->
                exerciseDao.updateExercise(
                    exerciseWithSets.exercise.copy(date = newDate.toString())
                )
            }
        }
    }

    // In ExerciseViewModel.kt
    fun addExercise(
        date: LocalDate,
        name: String,
        weight: String,
        repsOrDuration: String,
        notes: String,
        additionalSets: List<ExerciseSet> = emptyList()
    ) {
        viewModelScope.launch {
            // Get current exercises for ordering
            val currentExercises = exerciseDao.getExercisesByDate(date.toString())
            val maxOrder = currentExercises.maxOfOrNull { it.exercise.order } ?: -1

            val exercise = Exercise(
                date = date.toString(),
                name = name,
                order = maxOrder + 1
            )

            // Insert exercise and get ID
            val exerciseId = exerciseDao.insertExercise(exercise)

            // Create first set
            val firstSet = ExerciseSet(
                exerciseId = exerciseId.toInt(),
                weight = weight,
                repsOrDuration = repsOrDuration,
                notes = notes,
                order = 0
            )

            // Create list of all sets to insert at once
            val allSets = mutableListOf(firstSet)
            allSets.addAll(
                additionalSets.mapIndexed { index, set ->
                    set.copy(
                        exerciseId = exerciseId.toInt(),
                        order = index + 1
                    )
                }
            )

            // Insert all sets in a single transaction
            setDao.insertSets(allSets)
        }
    }

    fun deleteWorkout(date: LocalDate) {
        viewModelScope.launch {
            exerciseDao.deleteExercisesByDate(date.toString())
        }
    }

    fun deleteExercise(exercise: ExerciseWithSets) {
        viewModelScope.launch {
            exerciseDao.deleteExercise(exercise.exercise)
        }
    }

    fun updateExercise(exerciseWithSets: ExerciseWithSets) {
        viewModelScope.launch {
            exerciseDao.updateExercise(exerciseWithSets.exercise)
            exerciseWithSets.sets.forEach { set ->
                setDao.updateSet(set)
            }
        }
    }

    fun reorderExercises(exercises: List<ExerciseWithSets>) {
        viewModelScope.launch {
            exerciseDao.updateExercises(exercises.map { it.exercise })
        }
    }

    fun getExercisesForDate(date: LocalDate): Flow<List<ExerciseWithSets>> {
        return exerciseDao.getAllExercises()
            .map { exercises ->
                exercises
                    .filter { it.exercise.date == date.toString() }
                    .sortedBy { it.exercise.order }
            }
    }

    fun getExerciseById(id: Int): Flow<ExerciseWithSets?> {
        return exerciseDao.getExerciseByIdFlow(id)
    }

    fun updateExerciseWithSets(
        exerciseId: Int,
        name: String,
        sets: List<SetState>
    ) {
        viewModelScope.launch {
            // Get current exercise
            val currentExercise = exerciseDao.getExerciseById(exerciseId)
            if (currentExercise != null) {
                // Update exercise name
                val updatedExercise = currentExercise.exercise.copy(name = name)
                exerciseDao.updateExercise(updatedExercise)

                // Get existing sets
                val existingSets = currentExercise.sets

                // Create a transaction to handle all set operations
                setDao.updateSetsForExercise(
                    exerciseId = exerciseId,
                    newSets = sets.mapIndexed { index, setState ->
                        ExerciseSet(
                            id = if (index < existingSets.size) existingSets[index].id else 0,
                            exerciseId = exerciseId,
                            weight = setState.weight,
                            repsOrDuration = setState.repsOrDuration,
                            notes = setState.notes,
                            order = index
                        )
                    }
                )
            }
        }
    }

    fun deleteSet(exerciseId: Int, setId: Int) {
        viewModelScope.launch {
            val exercise = exerciseDao.getExerciseById(exerciseId)
            if (exercise != null && exercise.sets.size > 1) {
                // Delete the set
                setDao.deleteSetById(setId)

                // Reorder remaining sets
                val remainingSets = exercise.sets
                    .filter { it.id != setId }
                    .mapIndexed { index, set ->
                        set.copy(order = index)
                    }
                setDao.updateSets(remainingSets)
            }
        }
    }
}

class ExerciseViewModelFactory(
    private val exerciseDao: ExerciseDao,
    private val setDao: ExerciseSetDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(exerciseDao, setDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}