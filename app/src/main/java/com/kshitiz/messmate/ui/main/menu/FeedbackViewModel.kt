package com.kshitiz.messmate.ui.main.menu

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FeedbackViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _submitState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val submitState: StateFlow<Resource<String>> = _submitState

    fun submitFeedback(mealType: String, rating: Int, comment: String) {
        val user = auth.currentUser
        if (user == null) {
            _submitState.value = Resource.Error("User not logged in")
            return
        }

        if (rating == 0) {
            _submitState.value = Resource.Error("Please select a rating")
            return
        }

        _submitState.value = Resource.Loading

        // Simple Sentiment Logic
        val sentiment = when (rating) {
            in 4..5 -> "Positive"
            in 1..2 -> "Negative"
            else -> "Neutral"
        }

        val feedbackData = hashMapOf(
            "userId" to user.uid,
            "userEmail" to (user.email ?: "Anonymous"),
            "mealType" to mealType,
            "rating" to rating,
            "comment" to comment,
            "sentiment" to sentiment,
            "timestamp" to FieldValue.serverTimestamp()
        )

        firestore.collection("feedback")
            .add(feedbackData)
            .addOnSuccessListener {
                _submitState.value = Resource.Success("Feedback submitted successfully")
            }
            .addOnFailureListener { e ->
                _submitState.value = Resource.Error(e.message ?: "Submission failed", e)
            }
    }

    fun resetState() {
        _submitState.value = Resource.Idle
    }
}
