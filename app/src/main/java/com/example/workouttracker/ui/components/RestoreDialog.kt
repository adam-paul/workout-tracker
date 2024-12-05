// RestoreDialog.kt
package com.example.workouttracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RestoreDialog(
    backups: List<File>,
    onDismiss: () -> Unit,
    onRestore: (File) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Select Backup",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (backups.isEmpty()) {
                    Text("No backups available")
                } else {
                    LazyColumn {
                        items(backups) { backup ->
                            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                .format(Date(backup.lastModified()))

                            RetroButton(
                                onClick = { onRestore(backup) },
                                text = date,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                RetroButton(
                    onClick = onDismiss,
                    text = "Cancel",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
