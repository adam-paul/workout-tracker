package com.example.workouttracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.model.Exercise
import com.example.workouttracker.ui.components.RetroButton
import org.burnoutcrew.reorderable.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateScreen(
    date: LocalDate,
    exercises: List<Exercise>,
    onAddExercise: () -> Unit,
    onBack: () -> Unit,
    onDeleteExercise: (Exercise) -> Unit,
    onEditExercise: (Exercise) -> Unit,
    onReorderExercises: (List<Exercise>) -> Unit
) {
    val exerciseList = remember { mutableStateListOf<Exercise>().apply { addAll(exercises) } }

    LaunchedEffect(exercises) {
        val newExerciseIds = exercises.map { it.id }.toSet()
        val currentExerciseIds = exerciseList.map { it.id }.toSet()
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
            val updatedExercises = exerciseList.mapIndexed { index, exercise ->
                exercise.copy(order = index)
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
            items(exerciseList, key = { it.id }) { exercise ->
                ReorderableItem(state, key = exercise.id) { isDragging ->
                    val elevation = if (isDragging) 8.dp else 1.dp
                    val backgroundColor = if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.White
                    val scale = if (isDragging) 1.03f else 1f

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDragging) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Drag Handle",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.width(24.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Weight: ${exercise.weight}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                                Text(
                                    text = "Reps/Duration: ${exercise.repsOrDuration}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                            Row {
                                IconButton(onClick = { onEditExercise(exercise) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Exercise")
                                }
                                IconButton(onClick = { onDeleteExercise(exercise) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Exercise")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}