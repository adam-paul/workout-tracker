package dev.elgielabs.workoutlog.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun MonthConnector(isFirstItem: Boolean, isLastItem: Boolean) {
    val lineColor = MaterialTheme.colorScheme.onBackground

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isFirstItem) 72.dp else 48.dp)  // Taller for first item
    ) {
        val verticalLineX = 24.dp.toPx()
        val horizontalLineEndX = verticalLineX + 16.dp.toPx()

        // Draw vertical line
        drawLine(
            color = lineColor,
            start = Offset(verticalLineX, if (isFirstItem) 0f else 0f),
            end = Offset(verticalLineX, if (isLastItem) size.height / 2 else size.height),
            strokeWidth = 2f
        )

        // Draw horizontal stem at button center
        drawLine(
            color = lineColor,
            start = Offset(verticalLineX, if (isFirstItem) size.height - (size.height / 2) else size.height / 2),
            end = Offset(horizontalLineEndX, if (isFirstItem) size.height - (size.height / 2) else size.height / 2),
            strokeWidth = 2f
        )
    }
}