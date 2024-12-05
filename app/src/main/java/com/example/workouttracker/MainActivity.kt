package com.example.workouttracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workouttracker.data.database.BackupManager
import com.example.workouttracker.data.database.ExerciseDatabase
import com.example.workouttracker.data.model.ExerciseSet
import com.example.workouttracker.ui.components.MenuFab
import com.example.workouttracker.ui.components.AddFab
import com.example.workouttracker.ui.screens.*
import com.example.workouttracker.ui.theme.WorkoutTrackerTheme
import com.example.workouttracker.viewmodel.ExerciseViewModel
import com.example.workouttracker.viewmodel.ExerciseViewModelFactory
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    companion object {
        private val screensWithoutFAB = listOf("addExercise", "editExercise", "editDate")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = ExerciseDatabase.getDatabase(applicationContext)
        val backupManager = BackupManager(applicationContext).apply {
            registerForActivityResult(this@MainActivity)
        }

        setContent {
            WorkoutTrackerTheme {
                val navController = rememberNavController()
                val viewModel: ExerciseViewModel = viewModel(
                    factory = ExerciseViewModelFactory(
                        database.exerciseDao(),
                        database.exerciseSetDao()
                    )
                )

                var currentRoute by remember { mutableStateOf("main") }
                var currentDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

                navController.addOnDestinationChangedListener { _, destination, arguments ->
                    currentRoute = destination.route ?: ""
                    currentDate = arguments?.getString("date")?.let { LocalDate.parse(it) }
                }

                Scaffold { padding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = "main",
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable("main") {
                                val exercisesByMonth by viewModel.exercisesByMonth.collectAsState(
                                    initial = emptyMap()
                                )
                                MainScreen(
                                    exercisesByMonth = exercisesByMonth,
                                    expandedMonths = viewModel.expandedMonths,
                                    onToggleMonth = viewModel::toggleMonth,
                                    onDateSelected = { date ->
                                        navController.navigate("date/$date")
                                    },
                                    onDeleteWorkout = viewModel::deleteWorkout,
                                    onEditWorkoutDate = { date ->
                                        navController.navigate("editDate/$date")
                                    }
                                )
                            }

                            composable(
                                route = "date/{date}",
                                arguments = listOf(navArgument("date") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val date = LocalDate.parse(backStackEntry.arguments?.getString("date"))
                                val exercises by viewModel.getExercisesForDate(date)
                                    .collectAsState(initial = emptyList())

                                DateScreen(
                                    date = date,
                                    exercises = exercises,
                                    onAddExercise = {
                                        navController.navigate("addExercise/$date")
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onDeleteExercise = viewModel::deleteExercise,
                                    onEditExercise = { exercise ->
                                        navController.navigate("editExercise/${exercise.exercise.id}")
                                    },
                                    onReorderExercises = viewModel::reorderExercises,
                                    onNavigateUp = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(
                                route = "addExercise/{date}",
                                arguments = listOf(navArgument("date") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val date =
                                    LocalDate.parse(backStackEntry.arguments?.getString("date"))

                                AddEditExerciseScreen(
                                    onExerciseAdded = { name, weight, repsOrDuration, notes, additionalSets ->
                                        val convertedSets = additionalSets.map { set ->
                                            ExerciseSet(
                                                exerciseId = 0,
                                                weight = set.weight,
                                                repsOrDuration = set.repsOrDuration,
                                                notes = set.notes
                                            )
                                        }

                                        viewModel.addExercise(
                                            date = date,
                                            name = name,
                                            weight = weight,
                                            repsOrDuration = repsOrDuration,
                                            notes = notes,
                                            additionalSets = convertedSets
                                        )
                                        navController.popBackStack()
                                    },
                                    onExerciseUpdated = { id, name, sets ->
                                        viewModel.updateExerciseWithSets(id, name, sets)
                                        navController.popBackStack()
                                    },
                                    onSetDeleted = viewModel::deleteSet,
                                    onCancel = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(
                                route = "editExercise/{exerciseId}",
                                arguments = listOf(navArgument("exerciseId") {
                                    type = NavType.IntType
                                })
                            ) { backStackEntry ->
                                val exerciseId = backStackEntry.arguments?.getInt("exerciseId")
                                    ?: return@composable
                                val exercise by viewModel.getExerciseById(exerciseId)
                                    .collectAsState(initial = null)

                                AddEditExerciseScreen(
                                    exercise = exercise,
                                    onExerciseAdded = { name, weight, repsOrDuration, notes, _ ->
                                        exercise?.let {
                                            viewModel.updateExercise(
                                                it.copy(
                                                    exercise = it.exercise.copy(name = name),
                                                    sets = it.sets.toMutableList().apply {
                                                        if (isNotEmpty()) {
                                                            this[0] = this[0].copy(
                                                                weight = weight,
                                                                repsOrDuration = repsOrDuration,
                                                                notes = notes
                                                            )
                                                        }
                                                    }
                                                )
                                            )
                                        }
                                        navController.popBackStack()
                                    },
                                    onExerciseUpdated = { id, name, sets ->
                                        viewModel.updateExerciseWithSets(id, name, sets)
                                        navController.popBackStack()
                                    },
                                    onSetDeleted = { currentExerciseId, setId ->
                                        viewModel.deleteSet(currentExerciseId, setId)
                                    },
                                    onCancel = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable(
                                route = "editDate/{date}",
                                arguments = listOf(navArgument("date") {
                                    type = NavType.StringType
                                })
                            ) { backStackEntry ->
                                val oldDate =
                                    LocalDate.parse(backStackEntry.arguments?.getString("date"))

                                EditWorkoutDateScreen(
                                    oldDate = oldDate,
                                    onDateUpdated = { newDate ->
                                        viewModel.updateWorkoutDate(oldDate, newDate)
                                        navController.popBackStack()
                                    },
                                    onCancel = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding)
                        ) {
                            AnimatedVisibility(
                                visible = screensWithoutFAB.none { currentRoute.startsWith(it) },
                                enter = fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                ),
                                exit = fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = FastOutSlowInEasing
                                    )
                                ),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Separate animation for MenuFab
                                    AnimatedVisibility(
                                        visible = currentRoute == "main",
                                        enter = fadeIn(
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        ),
                                        exit = fadeOut(
                                            animationSpec = tween(
                                                durationMillis = 300,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                    ) {
                                        MenuFab(
                                            onBackupCreated = { path ->
                                                if (path != null) {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "Backup created successfully",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                } else {
                                                    Toast.makeText(
                                                        applicationContext,
                                                        "Failed to create backup",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            },
                                            onBackupRestored = { recreate() },
                                            backupManager = backupManager
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Add FAB (always visible)
                                    AddFab(
                                        onAddWorkout = {
                                            if (currentRoute == "main") {
                                                navController.navigate("addExercise/${LocalDate.now()}")
                                            } else {
                                                val dateToUse = currentDate ?: LocalDate.now()
                                                navController.navigate("addExercise/$dateToUse")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}