package com.kshitiz.messmate

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kshitiz.messmate.ui.navigation.AppNavigation
import com.kshitiz.messmate.ui.theme.MessMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Create controller for window insets
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // Hide ONLY the status bar
        controller.hide(WindowInsetsCompat.Type.statusBars())

        // Optional: make it transient (status bar reappears with swipe from top)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        enableEdgeToEdge()
        // Configure system bar icon appearance to ensure content shows through
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        // Use dark icons only when background is light; our top area is primary color, so prefer light icons
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        setContent {
            MessMateTheme {
                AppNavigation()
            }
        }
    }
}
