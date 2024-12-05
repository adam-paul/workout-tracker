package com.example.workouttracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun MainMenuContent(
    showMenu: Boolean,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onDismissMenu: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismissMenu
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Dark",
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(end = 8.dp)
            )
            Switch(
                checked = !isDarkTheme,  // Inverted the check
                onCheckedChange = { onThemeChanged(!it) }  // Inverted the change
            )
            Text(
                "Light",
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        HorizontalDivider()

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