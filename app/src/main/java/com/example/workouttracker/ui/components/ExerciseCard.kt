package com.example.workouttracker.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workouttracker.data.model.ExerciseWithSets

@Composable
fun ExerciseCard(
    exerciseWithSets: ExerciseWithSets,
    isDragging: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Calculate expandability in a derivedStateOf to ensure recomposition
    val isExpandable by remember(exerciseWithSets) {
        derivedStateOf {
            val hasNotes = exerciseWithSets.sets.firstOrNull()?.notes?.isNotBlank() == true
            val hasAdditionalSets = exerciseWithSets.sets.size > 1
            hasNotes || hasAdditionalSets
        }
    }

    val backgroundColor = if (isDragging)
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    else
        MaterialTheme.colorScheme.surface

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = if (isDragging) 1.03f else 1f
                scaleY = if (isDragging) 1.03f else 1f
            }
            .padding(vertical = 2.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = backgroundColor)
    ) {
        Column {
            // Main content area
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = if (isExpanded) 0.dp else 16.dp
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDragging) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Drag Handle",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exerciseWithSets.exercise.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        exerciseWithSets.sets.firstOrNull()?.let { firstSet ->
                            Text(
                                text = "Weight: ${firstSet.weight}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "Reps/Duration: ${firstSet.repsOrDuration}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Exercise")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Exercise")
                        }
                    }
                }
            }

            if (isExpandable) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        exerciseWithSets.sets.firstOrNull()?.let { firstSet ->
                            if (firstSet.notes.isNotBlank()) {
                                Text(
                                    text = "Notes: ${firstSet.notes}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }

                        exerciseWithSets.sets.drop(1).forEachIndexed { index, set ->
                            if (index > 0) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                            Text(
                                text = "Set ${index + 2}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Weight: ${set.weight}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            Text(
                                text = "Reps/Duration: ${set.repsOrDuration}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                            if (set.notes.isNotBlank()) {
                                Text(
                                    text = "Notes: ${set.notes}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    )
                                )
                            }
                        }
                    }
                }

                Surface(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Show Less" else "Show More",
                            modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                        )
                    }
                }
            }
        }
    }
}