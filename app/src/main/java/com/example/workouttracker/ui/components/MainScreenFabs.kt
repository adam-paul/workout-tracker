// MainScreenFabs.kt
package com.example.workouttracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun MenuFab(
    onBackupCreated: (Boolean) -> Unit,
    onBackupRestored: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = { showMenu = true }
        ) {
            Icon(Icons.Default.Menu, "Menu")
        }

        MainMenuContent(
            showMenu = showMenu,
            onDismissMenu = { showMenu = false },
            onBackup = { onBackupCreated(true) },
            onRestore = onBackupRestored
        )
    }
}

@Composable
fun AddFab(
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