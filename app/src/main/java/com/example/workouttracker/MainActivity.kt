package com.example.workouttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.workouttracker.data.database.ExerciseDatabase
import com.example.workouttracker.ui.screens.*
import com.example.workouttracker.ui.theme.WorkoutTrackerTheme
import com.example.workouttracker.viewmodel.ExerciseViewModel
import com.example.workouttracker.viewmodel.ExerciseViewModelFactory
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = ExerciseDatabase.getDatabase(applicationContext)

        setContent {
            WorkoutTrackerTheme {
                val navController = rememberNavController()
                val viewModel: ExerciseViewModel = viewModel(
                    factory = ExerciseViewModelFactory(database.exerciseDao())
                )

                var currentRoute by remember { mutableStateOf("main") }
                var currentDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }
                
                navController.addOnDestinationChangedListener { _, destination, arguments ->
                    currentRoute = destination.route ?: ""
                    currentDate = arguments?.getString("date")?.let { LocalDate.parse(it) }
                }

                val screensWithoutFAB = listOf("addExercise", "editExercise")

                Scaffold(
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = screensWithoutFAB.none { currentRoute.startsWith(it) },
                            enter = fadeIn(tween(300, easing = FastOutSlowInEasing)),
                            exit = fadeOut(tween(300, easing = FastOutSlowInEasing))
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    val dateToUse = currentDate ?: LocalDate.now()
                                    navController.navigate("addExercise/$dateToUse")
                                }
                            ) {
                                Icon(Icons.Default.Add, "Add Workout")
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(padding)
                    ) {
                        composable("main") {
                            val exercisesByMonth by viewModel.exercisesByMonth.collectAsState(initial = emptyMap())
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
                            arguments = listOf(navArgument("date") { type = NavType.StringType })
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
                                    navController.navigate("editExercise/${exercise.id}")
                                },
                                onReorderExercises = viewModel::reorderExercises
                            )
                        }

                        composable(
                            route = "addExercise/{date}",
                            arguments = listOf(navArgument("date") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val date = LocalDate.parse(backStackEntry.arguments?.getString("date"))

                            AddEditExerciseScreen(
                                onExerciseAdded = { name, weight, repsOrDuration, notes ->
                                    viewModel.addExercise(date, name, weight, repsOrDuration, notes)
                                    navController.popBackStack()
                                },
                                onCancel = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "editExercise/{exerciseId}",
                            arguments = listOf(navArgument("exerciseId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val exerciseId = backStackEntry.arguments?.getInt("exerciseId") ?: return@composable
                            val exercise by viewModel.getExerciseById(exerciseId)
                                .collectAsState(initial = null)

                            AddEditExerciseScreen(
                                exercise = exercise,
                                onExerciseAdded = { name, weight, repsOrDuration, notes ->
                                    exercise?.let {
                                        viewModel.updateExercise(
                                            it.copy(
                                                name = name,
                                                weight = weight,
                                                repsOrDuration = repsOrDuration,
                                                notes = notes
                                            )
                                        )
                                    }
                                    navController.popBackStack()
                                },
                                onCancel = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "editDate/{date}",
                            arguments = listOf(navArgument("date") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val oldDate = LocalDate.parse(backStackEntry.arguments?.getString("date"))

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
                }
            }
        }
    }
}