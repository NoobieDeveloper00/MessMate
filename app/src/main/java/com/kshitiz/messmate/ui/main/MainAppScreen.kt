package com.kshitiz.messmate.ui.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kshitiz.messmate.ui.main.attendance.AttendanceScreen
import com.kshitiz.messmate.ui.main.menu.MealMenuScreen
import com.kshitiz.messmate.ui.main.menu.MenuScreen
import com.kshitiz.messmate.ui.profile.ProfileViewModel
import com.kshitiz.messmate.ui.profile.ProfileScreen as ProfileScreenComposable

// 1. UPDATED ICONS (Rounded & Modern)
sealed class MainScreenRoutes(val route: String, val label: String, val icon: ImageVector) {
    data object Menu : MainScreenRoutes("menu_home", "Home", Icons.Rounded.Home)
    data object Attendance : MainScreenRoutes("attendance", "Scan", Icons.Rounded.QrCodeScanner)
    data object Profile : MainScreenRoutes("profile", "Profile", Icons.Rounded.AccountCircle)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    onNavigateToFeedback: (String) -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navItems = listOf(
        MainScreenRoutes.Menu,
        MainScreenRoutes.Attendance,
        MainScreenRoutes.Profile
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent
        ) { innerPadding ->
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
                    MealMenuScreen(
                        mealType = mealType,
                        onFeedbackClick = { onNavigateToFeedback(mealType) }
                    )
                }

                composable(MainScreenRoutes.Attendance.route) { AttendanceScreen() }

                composable(MainScreenRoutes.Profile.route) { ProfileHost(onLogout = onLogout) }
            }
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val showBottomBar = navItems.any { it.route == currentRoute }

        if (showBottomBar) {
            LabeledBottomDock(
                items = navItems,
                currentRoute = currentRoute,
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun LabeledBottomDock(
    items: List<MainScreenRoutes>,
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val dockColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp) // Total height reserved for the dock + popups
    ) {
        // --- 1. Background Bar ---
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp) // Height of the solid bar
                .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = dockColor,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            // This row just holds the Labels at the bottom
            Row(
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp) // Padding for text from bottom edge
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val textColor = if (isSelected) activeColor else inactiveColor

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- 2. Floating Icons Layer ---
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp) // Push icons up above the text labels
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route

                // Animation: Pop Up
                val offsetY by animateDpAsState(
                    targetValue = if (isSelected) (-20).dp else 0.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                    label = "jump"
                )

                // Animation: Scale Up
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    label = "scale"
                )

                // 2. BIGGER Pop-Up Circle (64dp)
                Box(
                    modifier = Modifier
                        .offset(y = offsetY)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemClick(item.route) },
                    contentAlignment = Alignment.Center
                ) {
                    // Floating Circle Surface
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) activeColor else Color.Transparent,
                        modifier = Modifier
                            .size(if (isSelected) 64.dp else 48.dp) // Large Active Circle
                            .scale(scale)
                            .shadow(
                                elevation = if (isSelected) 10.dp else 0.dp,
                                shape = CircleShape,
                                spotColor = activeColor.copy(alpha = 0.6f)
                            ),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else inactiveColor,
                                modifier = Modifier.size(if (isSelected) 32.dp else 28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHost(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = org.koin.androidx.compose.koinViewModel()
) {
    ProfileScreenComposable(
        viewModel = viewModel,
        onLogout = onLogout
    )
}