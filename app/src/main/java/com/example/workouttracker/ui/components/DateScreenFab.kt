// DateScreenFab.kt
package com.example.workouttracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// In DateScreenFab.kt, simplify to:
@Composable
fun DateScreenFab(
    onAddWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onAddWorkout,
        modifier = modifier
    ) {
        Icon(Icons.Default.Add, "Add Workout")
    }
}
