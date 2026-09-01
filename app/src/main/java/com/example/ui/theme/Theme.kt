package com.example.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = RawGlassDarkBackground,
    primaryContainer = NeonBlue,
    onPrimaryContainer = RawTextPrimaryDark,
    secondary = NeonPurple,
    onSecondary = RawTextPrimaryDark,
    secondaryContainer = Color(0x338A2BE2),
    onSecondaryContainer = RawTextPrimaryDark,
    tertiary = NeonPink,
    onTertiary = RawTextPrimaryDark,
    background = RawGlassDarkBackground,
    onBackground = RawTextPrimaryDark,
    surface = RawGlassDarkSurface,
    onSurface = RawTextPrimaryDark,
    surfaceVariant = Color(0xFF161C33),
    onSurfaceVariant = RawTextSecondaryDark,
    outline = RawGlassCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00629E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF6750A4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDFF),
    onSecondaryContainer = Color(0xFF22005D),
    tertiary = Color(0xFFB3261E),
    onTertiary = Color.White,
    background = Color(0xFFF2F5FA),
    onBackground = Color(0xFF1A1D27),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1D27),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF73777F)
)

/**
 * Liquid Glass color palette that adapts to the active theme.
 * Screens and components should read from [quickDropColors] instead of
 * hard-coding dark-only values so Light / Dark / System all remain readable.
 */
data class QuickDropColors(
    val background: Color,
    val glassSurface: Color,
    val glassSurfaceStrong: Color,
    val glassBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentCyan: Color,
    val accentGreen: Color,
    val accentPurple: Color,
    val accentPink: Color,
    val isDark: Boolean
)

val DarkQuickDropColors = QuickDropColors(
    background = RawGlassDarkBackground,
    glassSurface = Color(0x1F1E293B),
    glassSurfaceStrong = Color(0xF00F172A),
    glassBorder = Color(0x33FFFFFF),
    textPrimary = RawTextPrimaryDark,
    textSecondary = RawTextSecondaryDark,
    textTertiary = RawTextTertiaryDark,
    accentCyan = NeonCyan,
    accentGreen = NeonGreen,
    accentPurple = NeonPurple,
    accentPink = NeonPink,
    isDark = true
)

val LightQuickDropColors = QuickDropColors(
    background = Color(0xFFF2F5FA),
    glassSurface = Color(0xE6FFFFFF),
    glassSurfaceStrong = Color(0xFAFFFFFF),
    glassBorder = Color(0x335F6B8A),
    textPrimary = Color(0xFF1A1D27),
    textSecondary = Color(0xFF5B6472),
    textTertiary = Color(0xFF8A93A3),
    accentCyan = Color(0xFF007A8C),
    accentGreen = Color(0xFF007A4D),
    accentPurple = Color(0xFF6A2DD0),
    accentPink = Color(0xFFC2185B),
    isDark = false
)

val LocalQuickDropColors = staticCompositionLocalOf { DarkQuickDropColors }

@Composable
@ReadOnlyComposable
fun quickDropColors(): QuickDropColors = LocalQuickDropColors.current

/**
 * Smoothly animates every color in [colors] so switching Light <-> Dark <-> System
 * fades the whole UI (backgrounds, text, borders, glass surfaces) instead of
 * snapping instantly.
 */
@Composable
private fun animateQuickDropColors(colors: QuickDropColors): QuickDropColors {
    val spec = tween<Color>(durationMillis = 350)
    return QuickDropColors(
        background = animateColorAsState(colors.background, spec, label = "bg").value,
        glassSurface = animateColorAsState(colors.glassSurface, spec, label = "glassSurface").value,
        glassSurfaceStrong = animateColorAsState(colors.glassSurfaceStrong, spec, label = "glassSurfaceStrong").value,
        glassBorder = animateColorAsState(colors.glassBorder, spec, label = "glassBorder").value,
        textPrimary = animateColorAsState(colors.textPrimary, spec, label = "textPrimary").value,
        textSecondary = animateColorAsState(colors.textSecondary, spec, label = "textSecondary").value,
        textTertiary = animateColorAsState(colors.textTertiary, spec, label = "textTertiary").value,
        accentCyan = animateColorAsState(colors.accentCyan, spec, label = "accentCyan").value,
        accentGreen = animateColorAsState(colors.accentGreen, spec, label = "accentGreen").value,
        accentPurple = animateColorAsState(colors.accentPurple, spec, label = "accentPurple").value,
        accentPink = animateColorAsState(colors.accentPink, spec, label = "accentPink").value,
        isDark = colors.isDark
    )
}

/**
 * Root theme for the whole app. [darkTheme] should already be resolved from the
 * persisted "Light / Dark / System" preference (see MainActivity) - this
 * composable applies it consistently to Material3, the Liquid Glass color set,
 * and the system status/navigation bars, with a smooth cross-fade between themes.
 */
@Composable
fun QuickDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val targetColors = if (darkTheme) DarkQuickDropColors else LightQuickDropColors
    val colors = animateQuickDropColors(targetColors)
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = targetColors.background.toArgb()
            window.navigationBarColor = targetColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalQuickDropColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
