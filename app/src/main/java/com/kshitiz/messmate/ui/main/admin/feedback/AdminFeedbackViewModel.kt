package com.kshitiz.messmate.ui.main.admin.feedback

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FeedbackSummary(
    val totalCount: Int = 0,
    val averageRating: Float = 0f,
    val positiveCount: Int = 0,
    val neutralCount: Int = 0,
    val negativeCount: Int = 0,
    val comments: List<FeedbackItem> = emptyList()
)

data class FeedbackItem(
    val userEmail: String,
    val rating: Int,
    val comment: String,
    val sentiment: String
)

class AdminFeedbackViewModel(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _summaryState = MutableStateFlow<Resource<FeedbackSummary>>(Resource.Loading)
    val summaryState: StateFlow<Resource<FeedbackSummary>> = _summaryState

    private var allFeedback = listOf<FeedbackItem>()

    init {
        // Load default (Breakfast) initially
        loadFeedback("Breakfast")
    }

    fun loadFeedback(mealType: String) {
        _summaryState.value = Resource.Loading

        firestore.collection("feedback")
            .whereEqualTo("mealType", mealType)
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.map { doc ->
                    FeedbackItem(
                        userEmail = doc.getString("userEmail") ?: "Anonymous",
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        comment = doc.getString("comment") ?: "",
                        sentiment = doc.getString("sentiment") ?: "Neutral"
                    )
                }

                allFeedback = items
                calculateSummary(items)
            }
            .addOnFailureListener { e ->
                _summaryState.value = Resource.Error(e.message ?: "Failed to fetch feedback", e)
            }
    }

    private fun calculateSummary(items: List<FeedbackItem>) {
        if (items.isEmpty()) {
            _summaryState.value = Resource.Success(FeedbackSummary())
            return
        }

        val total = items.size
        val avg = items.map { it.rating }.average().toFloat()
        val pos = items.count { it.sentiment == "Positive" }
        val neu = items.count { it.sentiment == "Neutral" }
        val neg = items.count { it.sentiment == "Negative" }

        _summaryState.value = Resource.Success(
            FeedbackSummary(
                totalCount = total,
                averageRating = avg,
                positiveCount = pos,
                neutralCount = neu,
                negativeCount = neg,
                comments = items.filter { it.comment.isNotBlank() } // Only show feedback with text
            )
        )
    }
}