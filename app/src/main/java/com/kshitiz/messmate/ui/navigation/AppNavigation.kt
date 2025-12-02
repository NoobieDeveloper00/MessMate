package com.kshitiz.messmate.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.kshitiz.messmate.LoginScreen
import com.kshitiz.messmate.ui.auth.CreateAccountScreen
import com.kshitiz.messmate.ui.auth.LoginDetailsScreen
import com.kshitiz.messmate.ui.auth.admin.AdminLoginScreen
import com.kshitiz.messmate.ui.main.AdminPortalScreen
import com.kshitiz.messmate.ui.main.MainScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Login : Screen("login")
    object LoginDetails : Screen("login_details")
    object CreateAccount : Screen("create_account")
    object AdminLogin : Screen("admin_login")
    object AdminPortal : Screen("admin_portal")
    object Main : Screen("main")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Decide start destination based on current Firebase user session
    val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val startDest = if (firebaseAuth.currentUser != null) {
        Screen.Main.route
    } else {
        Screen.Auth.route
    }

    NavHost(navController = navController, startDestination = startDest) {
        // --- AUTHENTICATION FLOW ---
        navigation(startDestination = Screen.Login.route, route = Screen.Auth.route) {

            // 1. Landing Screen (Choose Login, Signup, or Admin)
            composable(Screen.Login.route) {
                LoginScreen(
                    onSignInClick = { navController.navigate(Screen.LoginDetails.route) },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
                    onAdminLoginClick = { navController.navigate(Screen.AdminLogin.route) }
                )
            }

            // 2. User Login Screen (Enter Email/Pass)
            composable(Screen.LoginDetails.route) {
                LoginDetailsScreen(
                    onLoginSuccess = {
                        // Only navigate when the ViewModel reports success
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) }
                )
            }

            // 3. Create Account Screen
            composable(Screen.CreateAccount.route) {
                CreateAccountScreen(
                    onAccountCreatedSuccess = {
                        // Only navigate when the ViewModel reports success
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onLoginClick = { navController.navigate(Screen.LoginDetails.route) }
                )
            }

            // 4. Admin Login Screen
            composable(Screen.AdminLogin.route) {
                AdminLoginScreen(
                    onAdminLoginClick = {
                        navController.navigate(Screen.AdminPortal.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // --- MAIN APP (USER) FLOW ---
        composable(route = Screen.Main.route) {
            MainScreen()
        }

        // --- ADMIN PORTAL FLOW ---
        composable(route = Screen.AdminPortal.route) {
            AdminPortalScreen()
        }
    }
}