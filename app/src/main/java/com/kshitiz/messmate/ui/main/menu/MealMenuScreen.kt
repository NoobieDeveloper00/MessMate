package com.kshitiz.messmate.ui.main.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshitiz.messmate.ui.theme.MessMateTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

// Hardcoded data source for the menus
private val menus = mapOf(
    "Breakfast" to listOf("Pancakes", "Omelette", "Toast with Jam", "Coffee", "Orange Juice"),
    "Lunch" to listOf("Chicken Curry", "Steamed Rice", "Lentil Soup", "Salad", "Yogurt"),
    "Snacks" to listOf("Samosa", "Cookies", "Tea", "Fruit Chaat"),
    "Dinner" to listOf("Paneer Butter Masala", "Naan", "Dal Makhani", "Jeera Rice", "Ice Cream")
)

@Composable
fun MealMenuScreen(
    mealType: String,
    onFeedbackClick: () -> Unit,
    viewModel: MenuViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val optOutState by viewModel.optOutState.collectAsState()
    val optedOutMeals by viewModel.optedOutMeals.collectAsState()
    val mealKey = mealType.lowercase(Locale.ENGLISH)

    LaunchedEffect(optOutState) {
        when (optOutState) {
            is com.kshitiz.messmate.util.Resource.Success -> {
                val msg = (optOutState as com.kshitiz.messmate.util.Resource.Success<String>).data
                    ?: "Opted out"
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT)
                    .show()
            }
            is com.kshitiz.messmate.util.Resource.Error -> {
                val msg = (optOutState as com.kshitiz.messmate.util.Resource.Error).message
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT)
                    .show()
            }
            else -> {}
        }
    }

    val menuItems = menus[mealType] ?: listOf("Menu not available.")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            // This modifier tells the Box to ignore the bottom safe area (navigation bar)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        // --- Themed Header Text ---
        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Thin,
            fontSize = 40.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 40.dp)
        )

        // --- Menu Content Sheet ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Meal Type Sub-heading ---
            Text(
                text = mealType,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Thin,
                fontSize = 35.sp,
                modifier = Modifier.padding(top = 24.dp, bottom = 24.dp)
            )

            // --- Menu Items List ---
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                menuItems.forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Divider(
                        modifier = Modifier.width(100.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
            }

            // --- Opt-Out Action (placed just above Feedback) ---
            if (optedOutMeals.contains(mealKey)) {
                Text(
                    text = "Opted Out",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                Button(
                    onClick = { viewModel.optOutFromMeal(mealType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Opt Out")
                }
            }

            // --- Feedback Button ---
            Button(
                onClick = onFeedbackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    // This padding ensures the button is not hidden by the bottom nav bar
                    .navigationBarsPadding()
            ) {
                Text("Give Feedback")
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun MealMenuScreenPreview() {
    MessMateTheme {
        MealMenuScreen(mealType = "Breakfast", onFeedbackClick = {})
    }
}

