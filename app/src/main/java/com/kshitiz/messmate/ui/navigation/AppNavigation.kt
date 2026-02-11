package com.kshitiz.messmate.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.kshitiz.messmate.ui.screens.auth.LoginScreen
import com.kshitiz.messmate.ui.screens.auth.CreateAccountScreen
import com.kshitiz.messmate.ui.screens.auth.LoginDetailsScreen
import com.kshitiz.messmate.ui.screens.auth.AdminLoginScreen
import com.kshitiz.messmate.ui.screens.main.AdminPortalScreen
import com.kshitiz.messmate.ui.screens.main.MainScreen
import com.kshitiz.messmate.ui.screens.admin.scanner.AdminScannerScreen
import com.kshitiz.messmate.ui.screens.admin.menu.AdminMenuScreen
import com.kshitiz.messmate.ui.screens.feedback.FeedbackScreen
import com.kshitiz.messmate.ui.screens.admin.feedback.AdminFeedbackScreen
import com.kshitiz.messmate.ui.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Login : Screen("login")
    data object LoginDetails : Screen("login_details")
    data object CreateAccount : Screen("create_account")
    data object AdminLogin : Screen("admin_login")
    data object AdminPortal : Screen("admin_portal")
    data object Main : Screen("main")
    data object AdminScanner : Screen("admin_scanner")
    data object AdminFeedback : Screen("admin_feedback")
    data object AdminMenu : Screen("admin_menu")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = koinViewModel()
    val startDest = if (authViewModel.isLoggedIn()) Screen.Main.route else Screen.Auth.route

    NavHost(navController = navController, startDestination = startDest) {

        // --- AUTH FLOW ---
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
                    onLoginSuccess = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) }
                )
            }
            composable(Screen.CreateAccount.route) {
                CreateAccountScreen(
                    onAccountCreatedSuccess = {
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
            MainScreen(
                onNavigateToFeedback = { mealType ->
                    navController.navigate("feedback/$mealType")
                },
                onLogout = {
                    authViewModel.logout()

                    // Navigate back to Login and clear stack
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // --- ADMIN PORTAL ---
        composable(route = Screen.AdminPortal.route) {
            AdminPortalScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Auth.route) { popUpTo(0) { inclusive = true } }
                },
                onNavigateToScanner = { navController.navigate(Screen.AdminScanner.route) },
                onNavigateToFeedback = { navController.navigate(Screen.AdminFeedback.route) },
                onNavigateToMenu = { navController.navigate(Screen.AdminMenu.route) }
            )
        }

        // --- ROOT LEVEL SCREENS ---
        composable(route = Screen.AdminScanner.route) {
            AdminScannerScreen()
        }

        composable(Screen.AdminMenu.route) {
            AdminMenuScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "feedback/{mealType}",
            arguments = listOf(androidx.navigation.navArgument("mealType") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType") ?: "Unknown"
            FeedbackScreen(
                mealType = mealType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminFeedback.route) {
            AdminFeedbackScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}