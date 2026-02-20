package com.kshitiz.messmate.ui.screens.main

import com.kshitiz.messmate.ui.screens.menu.MenuScreen
import com.kshitiz.messmate.ui.screens.menu.MealMenuScreen
import com.kshitiz.messmate.ui.screens.attendance.AttendanceScreen
import com.kshitiz.messmate.ui.screens.profile.ProfileScreen
import com.kshitiz.messmate.ui.screens.feedback.FeedbackScreen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kshitiz.messmate.ui.viewmodel.ProfileViewModel
import com.kshitiz.messmate.ui.screens.profile.ProfileScreen as ProfileScreenComposable

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
        MainScreenRoutes.Attendance,
        MainScreenRoutes.Menu,
        MainScreenRoutes.Profile
    )

    val routeIndex = mapOf(
        MainScreenRoutes.Attendance.route to 0,
        MainScreenRoutes.Menu.route to 1,
        MainScreenRoutes.Profile.route to 2
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainScreenRoutes.Menu.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    val from = routeIndex[initialState.destination.route] ?: 1
                    val to = routeIndex[targetState.destination.route] ?: 1
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> if (to >= from) fullWidth else -fullWidth },
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    val from = routeIndex[initialState.destination.route] ?: 1
                    val to = routeIndex[targetState.destination.route] ?: 1
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> if (to >= from) -fullWidth else fullWidth },
                        animationSpec = tween(300)
                    )
                },
                popEnterTransition = {
                    val from = routeIndex[initialState.destination.route] ?: 1
                    val to = routeIndex[targetState.destination.route] ?: 1
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> if (to >= from) fullWidth else -fullWidth },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    val from = routeIndex[initialState.destination.route] ?: 1
                    val to = routeIndex[targetState.destination.route] ?: 1
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> if (to >= from) -fullWidth else fullWidth },
                        animationSpec = tween(300)
                    )
                }
            ) {
                composable(MainScreenRoutes.Menu.route) {
                    MenuScreen(onMealCardClick = { mealType ->
                        navController.navigate("meal_menu/$mealType")
                    })
                }

                composable(
                    route = "meal_menu/{mealType}",
                    arguments = listOf(navArgument("mealType") { type = NavType.StringType }),
                    enterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(300)
                        )
                    },
                    exitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(300)
                        )
                    },
                    popEnterTransition = {
                        slideInHorizontally(
                            initialOffsetX = { fullWidth -> -fullWidth },
                            animationSpec = tween(300)
                        )
                    },
                    popExitTransition = {
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = tween(300)
                        )
                    }
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
                    val currentEntry = navController.currentBackStackEntry
                    if (currentEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
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

    val navBarInsets = WindowInsets.navigationBars
    val density = LocalDensity.current
    val navBarBottomPadding = with(density) { navBarInsets.getBottom(this).toDp() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp + navBarBottomPadding)
            .padding(bottom = navBarBottomPadding)
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

                val smoothSpring = spring<Dp>(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )

                // Animation: Pop Up
                val offsetY by animateDpAsState(
                    targetValue = if (isSelected) (-20).dp else 0.dp,
                    animationSpec = smoothSpring,
                    label = "jump"
                )

                // Animation: Scale Up
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )

                // Animation: Circle size
                val circleSize by animateDpAsState(
                    targetValue = if (isSelected) 64.dp else 48.dp,
                    animationSpec = smoothSpring,
                    label = "circleSize"
                )

                // Animation: Icon size
                val iconSize by animateDpAsState(
                    targetValue = if (isSelected) 32.dp else 28.dp,
                    animationSpec = smoothSpring,
                    label = "iconSize"
                )

                // Animation: Shadow elevation
                val shadowElevation by animateDpAsState(
                    targetValue = if (isSelected) 10.dp else 0.dp,
                    animationSpec = smoothSpring,
                    label = "shadow"
                )

                Box(
                    modifier = Modifier
                        .offset(y = offsetY)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemClick(item.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) activeColor else Color.Transparent,
                        modifier = Modifier
                            .size(circleSize)
                            .scale(scale)
                            .shadow(
                                elevation = shadowElevation,
                                shape = CircleShape,
                                spotColor = activeColor.copy(alpha = 0.6f)
                            ),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else inactiveColor,
                                modifier = Modifier.size(iconSize)
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