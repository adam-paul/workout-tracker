package com.example.workouttracker.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = MidForestGreen,
    secondary = DarkForestGreen,
    tertiary = LightForestGreen,
    surface = Color(0xFF1C1B1F),
    background = Color(0xFF1C1B1F),
    onSurface = Color.White,
    onBackground = Color.White
)

private val LightColors = lightColorScheme(
    primary = MidForestGreen,
    secondary = DarkForestGreen,
    tertiary = LightForestGreen,
    primaryContainer = MidForestGreen,
    onPrimaryContainer = LightForestGreen,
    surface = Color.White,
    background = Color.White,
    onSurface = Color.Black,
    onBackground = Color.Black
)

// Theme state management
object ThemeState {
    private var _isDarkTheme = mutableStateOf(false)
    var isDarkTheme: Boolean
        get() = _isDarkTheme.value
        set(value) {
            _isDarkTheme.value = value
        }

    // Add reference to ThemeManager
    private lateinit var themeManager: ThemeManager

    fun initialize(themeManager: ThemeManager) {
        this.themeManager = themeManager
    }

    suspend fun setTheme(isDark: Boolean) {
        if (::themeManager.isInitialized) {
            themeManager.setDarkTheme(isDark)
            isDarkTheme = isDark
        }
    }
}

@Composable
fun WorkoutTrackerTheme(
    darkTheme: Boolean = ThemeState.isDarkTheme,  // Updated to use ThemeState
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
