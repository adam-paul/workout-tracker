package com.example.workouttracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.workouttracker.ui.components.RetroButton
import com.example.workouttracker.data.model.Exercise

@Composable
fun AddEditExerciseScreen(
    exercise: Exercise? = null,
    onExerciseAdded: (name: String, weight: String, repsOrDuration: String, notes: String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var weight by remember { mutableStateOf(exercise?.weight ?: "") }
    var repsOrDuration by remember { mutableStateOf(exercise?.repsOrDuration ?: "") }
    var notes by remember { mutableStateOf(exercise?.notes ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Exercise Name", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = repsOrDuration,
            onValueChange = { repsOrDuration = it },
            label = { Text("Reps/Duration", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes", fontFamily = FontFamily.Monospace) },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        onExerciseAdded(name, weight, repsOrDuration, notes)
                    }
                },
                text = if (exercise == null) "Add" else "Update",
                modifier = Modifier.weight(1f)
            )
        }
    }
}