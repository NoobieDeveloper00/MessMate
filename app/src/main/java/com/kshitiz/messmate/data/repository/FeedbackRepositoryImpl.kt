package com.kshitiz.messmate.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.kshitiz.messmate.data.FirestoreConstants
import com.kshitiz.messmate.domain.model.FeedbackItem
import com.kshitiz.messmate.domain.model.FeedbackSummary
import com.kshitiz.messmate.domain.repository.FeedbackRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FeedbackRepositoryImpl(
    private val firestore: FirebaseFirestore
) : FeedbackRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun submitFeedback(mealType: String, feedback: FeedbackItem): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val today = dateFormat.format(Calendar.getInstance().time)
            val feedbackData = hashMapOf(
                "userEmail" to feedback.studentEmail,
                "mealType" to mealType,
                "rating" to feedback.rating,
                "comment" to feedback.comment,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "date" to today,
                "sentiment" to when (feedback.rating) {
                    in 4..5 -> "Positive"
                    in 1..2 -> "Negative"
                    else -> "Neutral"
                }
            )
            firestore.collection(FirestoreConstants.COLLECTION_FEEDBACK)
                .add(feedbackData)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to submit feedback")
        }
    }

    override fun getFeedbackSummary(mealType: String, date: String): Flow<Resource<FeedbackSummary>> = callbackFlow {
        trySend(Resource.Loading)
        val subscription = firestore.collection(FirestoreConstants.COLLECTION_FEEDBACK)
            .whereEqualTo("mealType", mealType)
            .whereEqualTo("date", date)
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
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteOldFeedback(): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -8)
            val cutoffDate = dateFormat.format(calendar.time)

            val oldDocs = firestore.collection(FirestoreConstants.COLLECTION_FEEDBACK)
                .whereLessThan("date", cutoffDate)
                .get()
                .await()

            if (oldDocs.isEmpty) return@withContext Resource.Success(Unit)

            val batch = firestore.batch()
            oldDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete old feedback")
        }
    }
}
