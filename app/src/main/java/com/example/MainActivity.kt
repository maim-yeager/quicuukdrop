package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.QuickDropNavGraph
import com.example.ui.theme.LocalHapticsEnabled
import com.example.ui.theme.QuickDropTheme
import com.example.ui.theme.quickDropColors
import com.example.ui.viewmodels.QuickDropViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Single shared ViewModel instance for the whole app (Activity-scoped),
            // passed down explicitly so MainActivity and the nav graph never
            // accidentally end up with two separate instances.
            val viewModel: QuickDropViewModel = viewModel()

            // Resolve the persisted "Light / Dark / System" preference into an
            // actual dark/light boolean. This is the fix for theme switching not
            // applying: previously the saved preference was never read here, so
            // the app always rendered with the OS default regardless of what the
            // user picked in Settings.
            val themeMode by viewModel.themeMode.collectAsState()
            val systemInDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> systemInDarkTheme // "SYSTEM" (and any unknown value) follows the OS setting
            }

            // Fix for the Haptic Feedback toggle in Settings doing nothing: it was
            // persisted but never actually read by the Liquid Glass components.
            val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()

            QuickDropTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalHapticsEnabled provides hapticsEnabled) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = quickDropColors().background
                    ) {
                        QuickDropNavGraph(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
