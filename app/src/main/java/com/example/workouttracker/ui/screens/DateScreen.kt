package com.example.workouttracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.model.ExerciseWithSets
import com.example.workouttracker.ui.components.DeleteConfirmationDialog
import com.example.workouttracker.ui.components.ExerciseCard
import com.example.workouttracker.ui.components.RetroButton
import org.burnoutcrew.reorderable.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateScreen(
    date: LocalDate,
    exercises: List<ExerciseWithSets>,
    onAddExercise: () -> Unit,
    onBack: () -> Unit,
    onDeleteExercise: (ExerciseWithSets) -> Unit,
    onEditExercise: (ExerciseWithSets) -> Unit,
    onReorderExercises: (List<ExerciseWithSets>) -> Unit,
    onNavigateUp: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var exerciseToDelete by remember { mutableStateOf<ExerciseWithSets?>(null) }
    val exerciseList = remember { mutableStateListOf<ExerciseWithSets>().apply { addAll(exercises) } }

    LaunchedEffect(exercises) {
        val newExerciseIds = exercises.map { it.exercise.id }.toSet()
        val currentExerciseIds = exerciseList.map { it.exercise.id }.toSet()
        if (newExerciseIds != currentExerciseIds) {
            exerciseList.clear()
            exerciseList.addAll(exercises)
        }
    }

    val state = rememberReorderableLazyListState(
        onMove = { from, to ->
            exerciseList.apply {
                if (to.index < size) {
                    add(to.index, removeAt(from.index))
                }
            }
        },
        onDragEnd = { _, _ ->
            val updatedExercises = exerciseList.mapIndexed { index, exerciseWithSets ->
                exerciseWithSets.copy(
                    exercise = exerciseWithSets.exercise.copy(order = index)
                )
            }
            onReorderExercises(updatedExercises)
        }
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RetroButton(
                onClick = onBack,
                text = "Back",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            RetroButton(
                onClick = onAddExercise,
                text = "Add Exercise",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            state = state.listState,
            modifier = Modifier
                .fillMaxSize()
                .reorderable(state)
                .detectReorderAfterLongPress(state)
        ) {
            items(exerciseList, key = { it.exercise.id }) { exerciseWithSets ->
                ReorderableItem(state, key = exerciseWithSets.exercise.id) { isDragging ->
                    ExerciseCard(
                        exerciseWithSets = exerciseWithSets,
                        isDragging = isDragging,
                        onEdit = { onEditExercise(exerciseWithSets) },
                        onDelete = {
                            exerciseToDelete = exerciseWithSets
                            showDeleteConfirmation = true
                        }
                    )
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        exerciseToDelete?.let { exercise ->
            DeleteConfirmationDialog(
                onConfirm = {
                    onDeleteExercise(exercise)
                    showDeleteConfirmation = false
                    exerciseToDelete = null
                    // Check if this was the last exercise
                    if (exerciseList.size <= 1) {
                        onNavigateUp()
                    }
                },
                onDismiss = {
                    showDeleteConfirmation = false
                    exerciseToDelete = null
                }
            )
        }
    }
}