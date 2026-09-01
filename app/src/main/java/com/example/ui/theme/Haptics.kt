package com.example.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Whether haptic feedback is enabled for interactive Liquid Glass components.
 * Populated from the persisted "Haptic Feedback" setting.
 */
val LocalHapticsEnabled = staticCompositionLocalOf { true }
