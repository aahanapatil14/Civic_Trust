package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PulseTeal,
    onPrimary = Slate900,
    primaryContainer = PulseTealDark,
    onPrimaryContainer = Color.White,
    secondary = PulseBlue,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = Slate50,
    surface = DarkSurface,
    onSurface = Slate100,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Slate200,
    error = HealthCritical,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PulseTealDark,
    onPrimary = Color.White,
    primaryContainer = PulseTeal,
    onPrimaryContainer = Slate900,
    secondary = PulseBlue,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = Slate900,
    surface = LightSurface,
    onSurface = Slate900,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Slate700,
    error = HealthCritical,
    onError = Color.White
)

@Composable
fun StructuralPulseTheme(
    darkTheme: Boolean = true, // Industrial dark UI is standard for 24/7 civic NOCs
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
