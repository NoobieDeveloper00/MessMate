package com.kshitiz.messmate.ui.screens.menu

import com.kshitiz.messmate.ui.viewmodel.MenuViewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kshitiz.messmate.ui.viewmodel.ProfileViewModel
import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.ui.theme.MessMateTheme
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class MealOption(val title: String, val icon: ImageVector, val color: Color)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenuScreen(
    onMealCardClick: (String) -> Unit,
    viewModel: MenuViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel()
) {
    // --- Data Setup ---
    val allMeals = listOf(
        MealOption("Breakfast", Icons.Default.Brightness5, Color(0xFFFFB74D)), // Orange
        MealOption("Lunch", Icons.Default.Restaurant, Color(0xFF4DB6AC)),      // Teal
        MealOption("Snacks", Icons.Default.Fastfood, Color(0xFF9575CD)),       // Purple
        MealOption("Dinner", Icons.Default.DinnerDining, Color(0xFFE57373))    // Red
    )

    // Time Logic
    val currentHour = LocalTime.now().hour
    val currentMealIndex = when {
        currentHour < 10 -> 0 // Breakfast
        currentHour < 15 -> 1 // Lunch
        currentHour < 18 -> 2 // Snacks
        else -> 3             // Dinner
    }

    // Dynamic Greeting Logic
    val timeGreeting = when {
        currentHour < 12 -> "Good Morning,"
        currentHour < 17 -> "Good Afternoon,"
        else -> "Good Evening,"
    }

    val featuredMeal = allMeals[currentMealIndex]
    val otherMeals = allMeals.filter { it != featuredMeal }

    val currentDate = LocalDate.now()
    val formattedDate = currentDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.ENGLISH))

    val profileState by profileViewModel.profileState.collectAsState()
    val dailyMenu by viewModel.dailyMenu.collectAsState()

    // User Profile & Crash Fix
    val profile = (profileState as? com.kshitiz.messmate.util.Resource.Success<User>)?.data
    val rawName = profile?.name ?: ""
    val greetingName = if (rawName.isNotBlank()) rawName.substringBefore(" ") else "Student"

    val favouriteDish = profile?.favouriteMeal.orEmpty()
    val isFavAvailable = favouriteDish.isNotBlank() &&
            dailyMenu.values.flatten().any { it.contains(favouriteDish, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. Top Section (Safe Area for Notch) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Greeting Text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timeGreeting,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = greetingName, // Bigger and Bolder
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Avatar with Border
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = greetingName.first().toString(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date Badge (Pill Shape)
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = "TODAY • ${formattedDate.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // --- 2. Scrollable Content ---
        // Using Column with weight to fill remaining space
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Hero Section Title
            Text(
                text = "Up Next",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Hero Card
            HeroMealCard(
                meal = featuredMeal,
                isFavAvailable = isFavAvailable,
                onClick = { onMealCardClick(featuredMeal.title) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Grid Section Title
            Text(
                text = "Full Menu",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Horizontal Row for other meals
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                otherMeals.forEach { meal ->
                    MiniMealCard(
                        meal = meal,
                        modifier = Modifier.weight(1f),
                        onClick = { onMealCardClick(meal.title) }
                    )
                }
            }

            // Bottom spacing for navigation bar
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeroMealCard(
    meal: MealOption,
    isFavAvailable: Boolean,
    onClick: () -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(meal.color.copy(alpha = 0.8f), meal.color)
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
        // Add elevation for pop
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp, pressedElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp) // Slightly taller
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            // Big Decorative Icon
            Icon(
                imageVector = meal.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.15f),
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp) // Push partially off-screen
            )

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.TopStart)
            ) {
                // Icon Chip
                Surface(
                    color = Color.White.copy(alpha = 0.25f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = meal.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = meal.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to view menu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            if (isFavAvailable) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "★ FAVOURITE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniMealCard(
    meal: MealOption,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.height(130.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(meal.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = meal.icon,
                    contentDescription = null,
                    tint = meal.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = meal.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true)
@Composable
private fun ModernHomePreview() {
    MessMateTheme {
        MenuScreen(onMealCardClick = {})
    }
}