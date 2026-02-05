package com.kshitiz.messmate.domain.repository

import com.kshitiz.messmate.domain.model.FeedbackItem
import com.kshitiz.messmate.domain.model.FeedbackSummary
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {
    suspend fun submitFeedback(mealType: String, feedback: FeedbackItem): Resource<Unit>
    fun getFeedbackSummary(mealType: String): Flow<Resource<FeedbackSummary>>
}
