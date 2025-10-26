package com.kshitiz.messmate.ui.navigation

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

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Login : Screen("login")
    data object LoginDetails : Screen("login_details")
    data object CreateAccount : Screen("create_account")
    data object AdminLogin : Screen("admin_login")
    data object AdminPortal : Screen("admin_portal")
    data object Main : Screen("main")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Auth.route) {
        // --- AUTHENTICATION FLOW ---
        navigation(startDestination = Screen.Login.route, route = Screen.Auth.route) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onSignInClick = { navController.navigate(Screen.LoginDetails.route) },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
                    onAdminLoginClick = { navController.navigate(Screen.AdminLogin.route) }
                )
            }
            composable(Screen.LoginDetails.route) {
                LoginDetailsScreen(
                    onSignInClick = { _, _ ->
                        // On success, navigate to the main app graph
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) }
                )
            }
            composable(Screen.CreateAccount.route) {
                CreateAccountScreen(
                    onRegisterClick = { _, _, _ ->
                        // On success, navigate to the main app graph
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onLoginClick = { navController.navigate(Screen.LoginDetails.route) }
                )
            }
            composable(Screen.AdminLogin.route) {
                AdminLoginScreen(
                    onAdminLoginClick = { _, _ ->
                        // On success, navigate to the admin portal
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
            // This loads the Scaffold with the bottom navigation bar
            MainScreen()
        }

        // --- ADMIN PORTAL FLOW ---
        composable(route = Screen.AdminPortal.route) {
            AdminPortalScreen()
        }
    }
}
