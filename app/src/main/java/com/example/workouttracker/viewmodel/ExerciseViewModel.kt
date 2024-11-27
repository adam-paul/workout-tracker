package com.example.workouttracker.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.workouttracker.data.dao.ExerciseDao
import com.example.workouttracker.data.model.Exercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class ExerciseViewModel(private val dao: ExerciseDao) : ViewModel() {
    val exercisesByDate: Flow<Map<LocalDate, List<Exercise>>> = dao.getAllExercises()
        .map { exercises ->
            exercises.groupBy { LocalDate.parse(it.date) }
                .mapValues { (_, exercises) -> exercises.sortedBy { it.order } }
        }

    val exercisesByMonth: Flow<Map<YearMonth, Map<LocalDate, List<Exercise>>>> = dao.getAllExercises()
        .map { exercises ->
            exercises.groupBy { YearMonth.from(LocalDate.parse(it.date)) }
                .mapValues { (_, monthExercises) ->
                    monthExercises.groupBy { LocalDate.parse(it.date) }
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
            val exercises = dao.getExercisesByDate(oldDate.toString())
            exercises.forEach { exercise ->
                dao.updateExercise(exercise.copy(date = newDate.toString()))
            }
        }
    }

    fun addExercise(date: LocalDate, name: String, weight: String, repsOrDuration: String, notes: String) {
        viewModelScope.launch {
            val currentExercises = dao.getExercisesByDate(date.toString())
            val maxOrder = currentExercises.maxOfOrNull { it.order } ?: -1

            val exercise = Exercise(
                date = date.toString(),
                name = name,
                weight = weight,
                repsOrDuration = repsOrDuration,
                notes = notes,
                order = maxOrder + 1
            )
            dao.insertExercise(exercise)
        }
    }

    fun deleteWorkout(date: LocalDate) {
        viewModelScope.launch {
            dao.deleteExercisesByDate(date.toString())
        }
    }

    fun deleteExercise(exercise: Exercise) {
        viewModelScope.launch {
            dao.deleteExercise(exercise)
        }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch {
            dao.updateExercise(exercise)
        }
    }

    fun reorderExercises(exercises: List<Exercise>) {
        viewModelScope.launch {
            dao.updateExercises(exercises)
        }
    }

    fun getExercisesForDate(date: LocalDate): Flow<List<Exercise>> {
        return dao.getAllExercises()
            .map { exercises ->
                exercises
                    .filter { it.date == date.toString() }
                    .sortedBy { it.order }
            }
    }

    fun getExerciseById(id: Int): Flow<Exercise?> {
        return dao.getAllExercises()
            .map { exercises ->
                exercises.find { it.id == id }
            }
    }
}

class ExerciseViewModelFactory(private val dao: ExerciseDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}