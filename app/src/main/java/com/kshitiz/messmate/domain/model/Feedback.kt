package com.kshitiz.messmate.domain.model

data class FeedbackItem(
    val studentEmail: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Long = 0,
    val sentiment: String = "Neutral"
)

data class FeedbackSummary(
    val mealType: String = "",
    val averageRating: Double = 0.0,
    val totalFeedbacks: Int = 0,
    val ratingDistribution: Map<Int, Int> = emptyMap(),
    val feedbacks: List<FeedbackItem> = emptyList(),
    val positiveCount: Int = 0,
    val neutralCount: Int = 0,
    val negativeCount: Int = 0
)
