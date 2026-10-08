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
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF133B63),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = SafetyOrangeLight,
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF703D00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    surface = Color(0xFF0F172A),
    onSurface = Color(0xFFF1F5F9),
    background = Color(0xFF0A101D),
    onBackground = Color(0xFFF1F5F9)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E4F7),
    onPrimaryContainer = NavyBlueDark,
    secondary = SafetyOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0CC),
    onSecondaryContainer = Color(0xFF5A2A00),
    tertiary = ToolSteelGrey,
    background = Color(0xFFF4F7FA),
    onBackground = Color(0xFF191C1E),
    surface = Color.White,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFE5ECF4),
    onSurfaceVariant = Color(0xFF43474E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand colors consistent for Ferretería De La O
    content: @Composable () -> Unit,
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
