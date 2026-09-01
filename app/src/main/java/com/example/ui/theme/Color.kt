package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Raw literal palette. These are the fixed Dark-mode values used to build
// DarkColorScheme / DarkQuickDropColors in Theme.kt. Screens should NOT use
// these directly - see the theme-reactive properties further below, which
// share the same names screens already import and automatically follow
// whichever theme (Light / Dark / System) is currently active.
// ---------------------------------------------------------------------------
val RawGlassDarkBackground = Color(0xFF070913)
val RawGlassDarkSurface = Color(0xFF0E1224)
val GlassCardBackground = Color(0x3318213F)
val RawGlassCardBorder = Color(0x3D8E9FCD)
val GlassCardHighlight = Color(0x22FFFFFF)

// Neon & Brand Accent Colors
val NeonCyan = Color(0xFF00F0FF)
val NeonBlue = Color(0xFF2979FF)
val NeonPurple = Color(0xFF8A2BE2)
val NeonPink = Color(0xFFFF2A85)
val NeonMagenta = Color(0xFFE024C3)
val NeonGreen = Color(0xFF00E676)
val NeonOrange = Color(0xFFFF9100)
val NeonYellow = Color(0xFFFFD600)

// Raw Dark-mode text literals (feed DarkColorScheme / DarkQuickDropColors only)
val RawTextPrimaryDark = Color(0xFFF0F4FC)
val RawTextSecondaryDark = Color(0xFF94A3B8)
val RawTextTertiaryDark = Color(0xFF64748B)

// ---------------------------------------------------------------------------
// Theme-reactive colors. Every screen in the app already imports these exact
// names (TextPrimary, TextSecondary, TextTertiary, GlassDarkBackground) - by
// making them composable properties backed by the active QuickDropColors,
// the whole app now follows Light / Dark / System without touching each
// screen individually. This is the fix for theme switching not updating the
// UI (previously these were fixed dark-only constants).
// ---------------------------------------------------------------------------
val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalQuickDropColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalQuickDropColors.current.textSecondary

val TextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalQuickDropColors.current.textTertiary

/** Screen root background. Reactive despite the "Dark" name kept for source compatibility. */
val GlassDarkBackground: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalQuickDropColors.current.background

// Glass Gradients
val LiquidGlassGradient = Brush.linearGradient(
    colors = listOf(
        Color(0x38FFFFFF),
        Color(0x10FFFFFF),
        Color(0x05FFFFFF),
        Color(0x18FFFFFF)
    )
)

val LiquidSendGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0052D4),
        Color(0xFF4364F7),
        Color(0xFF6FB1FC)
    )
)

val LiquidReceiveGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF0BA360),
        Color(0xFF3CBA92),
        Color(0xFF30DD8A)
    )
)

val LiquidRadarGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0x0000F0FF),
        Color(0x2200F0FF),
        Color(0x888A2BE2),
        Color(0xDD00F0FF)
    )
)

val RainbowColors = listOf(
    Color(0xFF2979FF), // Blue
    Color(0xFF8A2BE2), // Purple
    Color(0xFFFF2A85), // Pink
    Color(0xFFFF3366), // Red
    Color(0xFFFF9100), // Orange
    Color(0xFFFFD600), // Yellow
    Color(0xFF00E676), // Green
    Color(0xFF00F0FF)  // Cyan
)
