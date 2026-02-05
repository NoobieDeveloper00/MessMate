package com.kshitiz.messmate.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kshitiz.messmate.domain.model.FeedbackItem
import com.kshitiz.messmate.domain.model.FeedbackSummary
import com.kshitiz.messmate.domain.repository.FeedbackRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FeedbackRepositoryImpl(
    private val firestore: FirebaseFirestore
) : FeedbackRepository {

    override suspend fun submitFeedback(mealType: String, feedback: FeedbackItem): Resource<Unit> {
        return try {
            val feedbackData = hashMapOf(
                "userEmail" to feedback.studentEmail,
                "mealType" to mealType,
                "rating" to feedback.rating,
                "comment" to feedback.comment,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "sentiment" to when (feedback.rating) {
                    in 4..5 -> "Positive"
                    in 1..2 -> "Negative"
                    else -> "Neutral"
                }
            )
            firestore.collection("feedback")
                .add(feedbackData)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to submit feedback")
        }
    }

    override fun getFeedbackSummary(mealType: String): Flow<Resource<FeedbackSummary>> = callbackFlow {
        trySend(Resource.Loading)
        val subscription = firestore.collection("feedback")
            .whereEqualTo("mealType", mealType)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching feedback"))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val items = snapshot.documents.map { doc ->
                        FeedbackItem(
                            studentEmail = doc.getString("userEmail") ?: "Anonymous",
                            rating = doc.getLong("rating")?.toInt() ?: 0,
                            comment = doc.getString("comment") ?: "",
                            timestamp = (doc.getTimestamp("timestamp")?.seconds ?: 0L) * 1000,
                            sentiment = doc.getString("sentiment") ?: "Neutral"
                        )
                    }
                    
                    val total = items.size
                    val avgRating = if (total > 0) items.sumOf { it.rating }.toDouble() / total else 0.0
                    val distribution = items.groupBy { it.rating }.mapValues { it.value.size }
                    
                    val positive = items.count { it.rating >= 4 }
                    val neutral = items.count { it.rating == 3 }
                    val negative = items.count { it.rating <= 2 }
                    
                    trySend(Resource.Success(
                        FeedbackSummary(
                            mealType = mealType,
                            averageRating = avgRating,
                            totalFeedbacks = total,
                            ratingDistribution = distribution,
                            feedbacks = items,
                            positiveCount = positive,
                            neutralCount = neutral,
                            negativeCount = negative
                        )
                    ))
                }
            }
        awaitClose { subscription.remove() }
    }
}
