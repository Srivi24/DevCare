package com.devcare.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Color Palette ──
private val Teal200 = Color(0xFF80CBC4)
private val Teal500 = Color(0xFF009688)
private val Teal700 = Color(0xFF00796B)
private val Green200 = Color(0xFFA5D6A7)
private val Green500 = Color(0xFF4CAF50)
private val Surface = Color(0xFFF5F5F5)
private val OnSurface = Color(0xFF1C1B1F)
private val SurfaceDark = Color(0xFF1C1B1F)
private val OnSurfaceDark = Color(0xFFE6E1E5)

private val LightColorScheme = lightColorScheme(
    primary = Teal500,
    onPrimary = Color.White,
    primaryContainer = Teal200,
    onPrimaryContainer = Teal700,
    secondary = Green500,
    onSecondary = Color.White,
    secondaryContainer = Green200,
    surface = Surface,
    onSurface = OnSurface,
    background = Color.White,
    onBackground = OnSurface,
    surfaceVariant = Color(0xFFE7E0EC),
    outline = Color(0xFF79747E)
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal200,
    onPrimary = Teal700,
    primaryContainer = Teal700,
    onPrimaryContainer = Teal200,
    secondary = Green200,
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF2E7D32),
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    background = Color(0xFF121212),
    onBackground = OnSurfaceDark,
    surfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF938F99)
)

@Composable
fun DevCareTheme(
    themeMode: String = "system",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
