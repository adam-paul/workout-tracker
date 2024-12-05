package com.example.workouttracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily

@Composable
fun MainMenuContent(
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismissMenu
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    "Create Backup",
                    fontFamily = FontFamily.Monospace
                )
            },
            onClick = {
                onBackup()
                onDismissMenu()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    "Restore Backup",
                    fontFamily = FontFamily.Monospace
                )
            },
            onClick = {
                onRestore()
                onDismissMenu()
            }
        )
    }
}