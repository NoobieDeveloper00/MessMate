package com.kshitiz.messmate.ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kshitiz.messmate.ui.main.menu.MealMenuScreen
import com.kshitiz.messmate.ui.main.menu.MenuScreen

// --- Sealed class for Bottom Navigation routes ---
sealed class MainScreenRoutes(val route: String, val label: String, val icon: ImageVector) {
    data object Menu : MainScreenRoutes("menu_home", "Menu", Icons.Default.RestaurantMenu)
    data object Attendance : MainScreenRoutes("attendance", "Attendance", Icons.Default.CheckCircle)
    data object Nutrition : MainScreenRoutes("nutrition", "Nutrition", Icons.Default.BarChart)
}

// --- Main Container with Scaffold and Bottom Navigation ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(
        MainScreenRoutes.Menu,
        MainScreenRoutes.Attendance,
        MainScreenRoutes.Nutrition
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // --- Nested Navigation Graph for the Main App ---
        NavHost(
            navController = navController,
            startDestination = MainScreenRoutes.Menu.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainScreenRoutes.Menu.route) {
                MenuScreen(onMealCardClick = { mealType ->
                    navController.navigate("meal_menu/$mealType")
                })
            }
            composable(
                route = "meal_menu/{mealType}",
                arguments = listOf(navArgument("mealType") { type = NavType.StringType })
            ) { backStackEntry ->
                val mealType = backStackEntry.arguments?.getString("mealType") ?: "Unknown"
                MealMenuScreen(mealType = mealType, onFeedbackClick = { /*TODO*/ })
            }
            composable(MainScreenRoutes.Attendance.route) { AttendanceScreen() }
            composable(MainScreenRoutes.Nutrition.route) { NutritionScreen() }
        }
    }
}

// --- Placeholder Screens ---
@Composable
fun AttendanceScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Attendance Screen")
    }
}

@Composable
fun NutritionScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nutrition Screen")
    }
}

