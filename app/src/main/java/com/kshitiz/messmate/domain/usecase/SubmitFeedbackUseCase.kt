package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.model.FeedbackItem
import com.kshitiz.messmate.domain.repository.FeedbackRepository
import com.kshitiz.messmate.util.Resource

class SubmitFeedbackUseCase(private val repository: FeedbackRepository) {
    suspend operator fun invoke(mealType: String, rating: Int, comment: String, email: String): Resource<Unit> {
        if (rating == 0) {
            return Resource.Error("Please provide a rating")
        }
        val feedback = FeedbackItem(
            studentEmail = email,
            rating = rating,
            comment = comment,
            timestamp = System.currentTimeMillis()
        )
        return repository.submitFeedback(mealType, feedback)
    }
}
