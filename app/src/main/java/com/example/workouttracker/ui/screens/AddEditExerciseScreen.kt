package com.example.workouttracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workouttracker.ui.components.RetroButton
import com.example.workouttracker.data.model.ExerciseWithSets
import com.example.workouttracker.data.model.SetState

@Composable
fun AddEditExerciseScreen(
    exercise: ExerciseWithSets? = null,
    onExerciseAdded: (name: String, weight: String, repsOrDuration: String, notes: String, additionalSets: List<SetState>) -> Unit,
    onExerciseUpdated: (exerciseId: Int, name: String, sets: List<SetState>) -> Unit,
    onSetDeleted: (exerciseId: Int, setId: Int) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf(listOf(SetState("", "", ""))) }
    val scrollState = rememberScrollState()

    LaunchedEffect(exercise) {
        if (exercise != null) {
            name = exercise.exercise.name
            sets = exercise.sets.map { set ->
                SetState(
                    weight = set.weight,
                    repsOrDuration = set.repsOrDuration,
                    notes = set.notes
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding()
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Exercise Name", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            sets.forEachIndexed { index, set ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set ${index + 1}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (index > 0) {
                        IconButton(
                            onClick = {
                                if (exercise != null && index < exercise.sets.size) {
                                    // This is an existing set, delete it through the ViewModel
                                    onSetDeleted(exercise.exercise.id, exercise.sets[index].id)
                                }
                                // Always update the local state regardless of whether it's a persisted set
                                sets = sets.toMutableList().apply {
                                    removeAt(index)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Set")
                        }
                    }
                }

                SetFields(
                    weight = set.weight,
                    repsOrDuration = set.repsOrDuration,
                    notes = set.notes,
                    onWeightChange = { sets = sets.toMutableList().apply { this[index] = set.copy(weight = it) } },
                    onRepsOrDurationChange = { sets = sets.toMutableList().apply { this[index] = set.copy(repsOrDuration = it) } },
                    onNotesChange = { sets = sets.toMutableList().apply { this[index] = set.copy(notes = it) } }
                )
            }

            RetroButton(
                onClick = { sets = sets + SetState("", "", "") },
                text = "+",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RetroButton(
                onClick = onCancel,
                text = "Cancel",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            RetroButton(
                onClick = {
                    if (name.isNotBlank()) {
                        if (exercise == null) {
                            // New exercise
                            onExerciseAdded(
                                name,
                                sets.first().weight,
                                sets.first().repsOrDuration,
                                sets.first().notes,
                                sets.drop(1)
                            )
                        } else {
                            // Update existing exercise
                            onExerciseUpdated(
                                exercise.exercise.id,
                                name,
                                sets
                            )
                        }
                    }
                },
                text = if (exercise == null) "Add" else "Update",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SetFields(
    weight: String,
    repsOrDuration: String,
    notes: String,
    onWeightChange: (String) -> Unit,
    onRepsOrDurationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    TextField(
        value = weight,
        onValueChange = onWeightChange,
        label = { Text("Weight", fontFamily = FontFamily.Monospace) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontFamily = FontFamily.Monospace)
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = repsOrDuration,
        onValueChange = onRepsOrDurationChange,
        label = { Text("Reps/Duration", fontFamily = FontFamily.Monospace) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontFamily = FontFamily.Monospace)
    )
    Spacer(modifier = Modifier.height(8.dp))
    TextField(
        value = notes,
        onValueChange = onNotesChange,
        label = { Text("Notes", fontFamily = FontFamily.Monospace) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(fontFamily = FontFamily.Monospace)
    )
}
