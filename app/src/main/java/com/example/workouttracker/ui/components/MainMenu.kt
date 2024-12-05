// MainMenu.kt
package com.example.workouttracker.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily
import com.example.workouttracker.data.database.BackupManager

@Composable
fun MainMenuContent(
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    backupManager: BackupManager,
    onBackupCreated: (String?) -> Unit,
    onBackupRestored: () -> Unit
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
                backupManager.initiateBackup(onBackupCreated)
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
                backupManager.initiateRestore(onBackupRestored)
                onDismissMenu()
            }
        )
    }
}
