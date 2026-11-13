package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PomodoroRed,
    onPrimary = BackgroundDark,
    primaryContainer = PomodoroRedDark,
    onPrimaryContainer = TextPrimary,
    secondary = BreakGreen,
    onSecondary = BackgroundDark,
    secondaryContainer = BreakGreenDark,
    onSecondaryContainer = TextPrimary,
    tertiary = LongBreakBlue,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PomodoroRedDark,
    onPrimary = TextPrimary,
    primaryContainer = PomodoroRed,
    secondary = BreakGreenDark,
    background = BackgroundDark, // Modo escuro padrão para imersão de foco
    surface = SurfaceDark,
    onSurface = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Padrão escuro imersivo para economia de bateria no Galaxy A15
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = BackgroundDark.toArgb()
                window.navigationBarColor = BackgroundDark.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
