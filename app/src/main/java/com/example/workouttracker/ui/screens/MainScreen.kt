package com.example.workouttracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.data.model.ExerciseWithSets
import com.example.workouttracker.ui.components.DeleteConfirmationDialog
import com.example.workouttracker.ui.components.MonthConnector
import com.example.workouttracker.ui.components.MonthHeader
import com.example.workouttracker.ui.components.RetroButton
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun MainScreen(
    exercisesByMonth: Map<YearMonth, Map<LocalDate, List<ExerciseWithSets>>>,
    expandedMonths: List<YearMonth>,
    onToggleMonth: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDeleteWorkout: (LocalDate) -> Unit,
    onEditWorkoutDate: (LocalDate) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var dateToDelete by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "WORKOUT LOG",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            exercisesByMonth
                .keys
                .sortedDescending()
                .forEach { month ->
                    item(key = "header_${month}") {
                        MonthHeader(
                            month = month,
                            isExpanded = expandedMonths.contains(month),
                            onToggle = { onToggleMonth(month) }
                        )
                    }

                    if (expandedMonths.contains(month)) {
                        val datesInMonth = exercisesByMonth[month] ?: emptyMap()
                        val sortedDates = datesInMonth.keys.sortedDescending()

                        sortedDates.forEachIndexed { index, date ->
                            item(key = "date_${month}_${date}") {
                                Box(modifier = Modifier.height(48.dp)) {
                                    MonthConnector(
                                        isFirstItem = index == 0,
                                        isLastItem = index == sortedDates.lastIndex
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 40.dp)
                                    ) {
                                        // Each RetroButton gets its own remembered state
                                        var showActionsForThis by remember(date) { mutableStateOf(false) }

                                        RetroButton(
                                            onClick = { onDateSelected(date) },
                                            onEdit = { onEditWorkoutDate(date) },
                                            onDelete = {
                                                dateToDelete = date
                                                showDeleteConfirmation = true
                                                showActionsForThis = true
                                            },
                                            text = "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)} | ${datesInMonth[date]?.size ?: 0} ${if ((datesInMonth[date]?.size ?: 0) == 1) "exercise" else "exercises"}",
                                            keepActionsVisible = showDeleteConfirmation && dateToDelete == date
                                        )
                                    }
                                }
                            }
                        }
                        item(key = "spacer_${month}") {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
        }
    }

    if (showDeleteConfirmation && dateToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                dateToDelete?.let { onDeleteWorkout(it) }
                showDeleteConfirmation = false
                dateToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                dateToDelete = null
            }
        )
    }
}
