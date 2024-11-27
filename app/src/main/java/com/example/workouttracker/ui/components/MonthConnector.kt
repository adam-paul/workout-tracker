package com.example.workouttracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MonthConnector(isFirstItem: Boolean, isLastItem: Boolean) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isFirstItem) 72.dp else 48.dp)  // Taller for first item
    ) {
        val verticalLineX = 24.dp.toPx()
        val horizontalLineEndX = verticalLineX + 16.dp.toPx()

        // Draw vertical line
        drawLine(
            color = Color.Black,
            start = Offset(verticalLineX, if (isFirstItem) 0f else 0f),
            end = Offset(verticalLineX, if (isLastItem) size.height / 2 else size.height),
            strokeWidth = 2f
        )

        // Draw horizontal stem at button center
        drawLine(
            color = Color.Black,
            start = Offset(verticalLineX, if (isFirstItem) size.height - (size.height / 2) else size.height / 2),
            end = Offset(horizontalLineEndX, if (isFirstItem) size.height - (size.height / 2) else size.height / 2),
            strokeWidth = 2f
        )
    }
}