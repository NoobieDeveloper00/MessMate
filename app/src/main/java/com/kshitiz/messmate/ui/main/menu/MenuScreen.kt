package com.kshitiz.messmate.ui.main.menu

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshitiz.messmate.ui.theme.MessMateTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Data class to hold information for each meal card
data class Meal(val title: String, val icon: ImageVector)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MenuScreen(onMealCardClick: (String) -> Unit) {
    val meals = listOf(
        Meal("Breakfast", Icons.Outlined.WbSunny),
        Meal("Lunch", Icons.Outlined.LunchDining),
        Meal("Snacks", Icons.Outlined.Cookie),
        Meal("Dinner", Icons.Outlined.DinnerDining)
    )

    // Get current date and format it
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)
    val formattedDate = currentDate.format(formatter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Header Section ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 10.dp)
        ) {
            Text(
                text = "Hello Kshitiz!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- Today's Special Section ---
        Text(
            text = "You have your favourite breakfast today!",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp)
        )


        // --- Centered Grid Container ---
        Box(
            modifier = Modifier
                .weight(1f) // This makes the Box take up all remaining space
                .fillMaxWidth(),
            contentAlignment = Alignment.Center // This centers the grid within the Box
        ) {
            // --- Meal Options Grid ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false // Since the grid fits on screen, scrolling is disabled
            ) {
                items(meals) { meal ->
                    MealCard(
                        meal = meal,
                        onClick = { onMealCardClick(meal.title) }
                    )
                }
            }
        }
    }
}

@Composable
fun MealCard(
    meal: Meal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f) // Makes the card a perfect square
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = meal.icon,
                contentDescription = meal.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = meal.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true, name = "Light Mode")
@Composable
private fun MenuScreenPreviewLight() {
    MessMateTheme(darkTheme = false) {
        MenuScreen(onMealCardClick = {})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true, name = "Dark Mode")
@Composable
private fun MenuScreenPreviewDark() {
    MessMateTheme(darkTheme = true) {
        MenuScreen(onMealCardClick = {})
    }
}

