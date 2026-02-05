package com.kshitiz.messmate.ui.screens.admin.feedback

import com.kshitiz.messmate.ui.viewmodel.AdminFeedbackViewModel
import com.kshitiz.messmate.domain.model.FeedbackSummary
import com.kshitiz.messmate.domain.model.FeedbackItem
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kshitiz.messmate.ui.theme.MessMateTheme
import com.kshitiz.messmate.util.Resource
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeedbackScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminFeedbackViewModel = koinViewModel()
) {
    MessMateTheme(darkTheme = true) {
        val state by viewModel.summaryState.collectAsState()
        var selectedMeal by remember { mutableStateOf("Breakfast") }
        val meals = listOf("Breakfast", "Lunch", "Snacks", "Dinner")

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            // FIX: Manual padding to avoid notch
            modifier = Modifier.statusBarsPadding(),
            topBar = {
                TopAppBar(
                    title = {
                        Text("Feedback Analytics", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    meals.forEach { meal ->
                        FilterChip(
                            selected = selectedMeal == meal,
                            onClick = {
                                selectedMeal = meal
                                viewModel.loadFeedback(meal)
                            },
                            label = { Text(meal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                when (val result = state) {
                    is Resource.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is Resource.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${result.message}", color = MaterialTheme.colorScheme.error) }
                    is Resource.Success -> {
                        val summary = result.data ?: FeedbackSummary()
                        FeedbackContent(summary)
                    }
                    else -> {}
                }
            }
        }
    }
}

// ... (Keep the FeedbackContent, SentimentChart, and CommentItem functions exactly as they were in the previous response, they were already correct for dark mode) ...
@Composable
fun FeedbackContent(summary: FeedbackSummary) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column {
                        Text("Total Reviews", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        Text("${summary.totalFeedbacks}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text("Avg Rating", style = MaterialTheme.typography.labelMedium, color = Color.White)
                        Text(String.format("%.1f ★", summary.averageRating), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    SentimentChart(summary)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Recent Comments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(12.dp))
        }

        val filteredComments = summary.feedbacks.filter { it.comment.isNotBlank() }

        if (filteredComments.isEmpty()) {
            item {
                Text(
                    "No written comments for this meal yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        } else {
            items(filteredComments) { item ->
                CommentItem(item)
            }
        }
    }
}

@Composable
fun SentimentChart(summary: FeedbackSummary) {
    if (summary.totalFeedbacks == 0) return

    val posColor = Color(0xFF4CAF50)
    val neuColor = Color(0xFFFFC107)
    val negColor = Color(0xFFF44336)

    val total = summary.totalFeedbacks.toFloat()
    val posSweep = (summary.positiveCount / total) * 360f
    val neuSweep = (summary.neutralCount / total) * 360f
    val negSweep = (summary.negativeCount / total) * 360f

    Canvas(modifier = Modifier.size(100.dp)) {
        val strokeWidth = 20f
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
        val size = Size(diameter, diameter)
        val style = Stroke(width = strokeWidth)

        var startAngle = -90f
        if (summary.positiveCount > 0) {
            drawArc(color = posColor, startAngle = startAngle, sweepAngle = posSweep, useCenter = false, topLeft = topLeft, size = size, style = style)
            startAngle += posSweep
        }
        if (summary.neutralCount > 0) {
            drawArc(color = neuColor, startAngle = startAngle, sweepAngle = neuSweep, useCenter = false, topLeft = topLeft, size = size, style = style)
            startAngle += neuSweep
        }
        if (summary.negativeCount > 0) {
            drawArc(color = negColor, startAngle = startAngle, sweepAngle = negSweep, useCenter = false, topLeft = topLeft, size = size, style = style)
        }
    }
}

@Composable
fun CommentItem(item: FeedbackItem) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.studentEmail.split("@")[0], fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Text("${item.rating} ★", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            Text(item.comment, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = item.sentiment,
                style = MaterialTheme.typography.labelSmall,
                color = when(item.sentiment) {
                    "Positive" -> Color(0xFF4CAF50)
                    "Negative" -> Color(0xFFF44336)
                    else -> Color.Gray
                },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}