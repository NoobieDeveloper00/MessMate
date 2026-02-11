package com.kshitiz.messmate.ui.screens.feedback

import com.kshitiz.messmate.ui.viewmodel.FeedbackViewModel

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshitiz.messmate.util.Resource
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    mealType: String,
    onNavigateBack: () -> Unit,
    viewModel: FeedbackViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val feedbackUiState by viewModel.uiState.collectAsState()
    val submitState = feedbackUiState.submitState

    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(submitState) {
        when (val state = submitState) {
            is Resource.Success -> {
                Toast.makeText(context, state.data, Toast.LENGTH_SHORT).show()
                onNavigateBack()
                viewModel.resetState()
            }
            is Resource.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // Elegant Gradient
    val headerGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(headerGradient)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
    ) {
        // --- 1. Header Area ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Back Button
            Row(
                modifier = Modifier.padding(top = 25.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Refined Title: Single line, elegant weight
                Text(
                    text = "Rate your $mealType",
                    style = MaterialTheme.typography.headlineLarge, // Smaller than display
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold, // Less bold than before
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // --- 2. Content Sheet ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp) // Adjusted height
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "How was the food?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Star Rating ---
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in 1..5) {
                    val isSelected = i <= rating
                    // Use Gold for selected, Soft Grey for unselected
                    val icon = if (isSelected) Icons.Filled.Star else Icons.Filled.StarOutline
                    val tint = if (isSelected) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                    Icon(
                        imageVector = icon,
                        contentDescription = "Star $i",
                        tint = tint,
                        modifier = Modifier
                            .size(52.dp)
                            .padding(4.dp)
                            .clickable { rating = i }
                    )
                }
            }

            // Rating Label
            val ratingLabel = when(rating) {
                1 -> "Awful"
                2 -> "Bad"
                3 -> "Okay"
                4 -> "Good"
                5 -> "Delicious!"
                else -> "Tap a star"
            }
            Text(
                text = ratingLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Comment Input ---
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Leave a comment (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                maxLines = 5
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Submit Confirmation Dialog ---
            var showSubmitDialog by remember { mutableStateOf(false) }

            if (showSubmitDialog) {
                AlertDialog(
                    onDismissRequest = { showSubmitDialog = false },
                    title = { Text("Submit Review") },
                    text = { Text("Are you sure you want to submit your review?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showSubmitDialog = false
                            viewModel.submitFeedback(mealType, rating, comment)
                        }) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSubmitDialog = false }) { Text("No") }
                    }
                )
            }

            // --- Submit Button ---
            Button(
                onClick = { showSubmitDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    // Changed to Primary (Mint) for consistency
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White // Force White text
                ),
                enabled = submitState !is Resource.Loading
            ) {
                if (submitState is Resource.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Submit Review",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}