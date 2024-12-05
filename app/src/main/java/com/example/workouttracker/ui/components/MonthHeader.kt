package com.example.workouttracker.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MonthHeader(
    month: YearMonth,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    RetroButton(
        onClick = onToggle,
        text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
    )
}