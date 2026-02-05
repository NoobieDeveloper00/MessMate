package com.kshitiz.messmate

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.kshitiz.messmate.ui.navigation.AppNavigation
import com.kshitiz.messmate.ui.theme.MessMateTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
