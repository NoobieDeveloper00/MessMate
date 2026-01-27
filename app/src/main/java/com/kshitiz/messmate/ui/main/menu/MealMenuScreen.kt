package com.kshitiz.messmate.ui.main.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.ThumbDownOffAlt
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshitiz.messmate.ui.theme.MessMateTheme
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun MealMenuScreen(
    mealType: String,
    onFeedbackClick: () -> Unit,
    viewModel: MenuViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val optOutState by viewModel.optOutState.collectAsState()
    val optedOutMeals by viewModel.optedOutMeals.collectAsState()
    val dailyMenu by viewModel.dailyMenu.collectAsState()
    val mealKey = mealType.lowercase(Locale.ENGLISH)

    LaunchedEffect(optOutState) {
        when (optOutState) {
            is com.kshitiz.messmate.util.Resource.Success -> {
                val msg = (optOutState as com.kshitiz.messmate.util.Resource.Success<String>).data ?: "Opted out"
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
            is com.kshitiz.messmate.util.Resource.Error -> {
                val msg = (optOutState as com.kshitiz.messmate.util.Resource.Error).message
                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val menuItems = dailyMenu[mealType] ?: listOf("Loading menu...")

    val mealIcon = when (mealType) {
        "Breakfast" -> Icons.Outlined.WbSunny
        "Lunch" -> Icons.Outlined.LunchDining
        "Snacks" -> Icons.Default.Fastfood
        "Dinner" -> Icons.Outlined.Cookie
        else -> Icons.Default.Restaurant
    }

    // Gradient Background for the "Premium" feel
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(padding)
        ) {
            // --- Back Button / Header Area ---
            // (You can add a back button here if needed, but swipe gesture works too)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                // --- THE MENU CARD ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Take up most space but leave room for buttons
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. Top Decoration
                        Icon(
                            imageVector = mealIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )

                        // 2. Title
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TODAY'S",
                                style = MaterialTheme.typography.labelMedium,
                                letterSpacing = 4.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mealType.uppercase(),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Thin,
                                letterSpacing = 2.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(
                                modifier = Modifier.width(60.dp),
                                thickness = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }

                        // 3. The Food List (Centered & Beautiful)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (menuItems.isEmpty() || (menuItems.size == 1 && menuItems[0].startsWith("Loading"))) {
                                Text(
                                    text = "Loading...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = Color.Gray
                                )
                            } else {
                                menuItems.forEachIndexed { index, item ->
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.headlineSmall, // Large text
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    // Add a small separator dot between items, but not after the last one
                                    if (index < menuItems.size - 1) {
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Bottom Decor (Chef's Kiss)
                        Text(
                            text = "Bon Appétit",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // --- ACTION BUTTONS (Floating below card) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Opt Out Button
                    if (optedOutMeals.contains(mealKey)) {
                        Button(
                            onClick = {},
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                                disabledContentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Outlined.ThumbDownOffAlt, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Skipped")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { viewModel.optOutFromMeal(mealType) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text("Skip Meal")
                        }
                    }

                    // Feedback Button
                    Button(
                        onClick = onFeedbackClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Outlined.Feedback, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Feedback")
                    }
                }
                Spacer(Modifier.height(16.dp)) // Extra bottom padding
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun MealMenuScreenPreview() {
    MessMateTheme {
        MealMenuScreen(mealType = "Lunch", onFeedbackClick = {})
    }
}