package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.model.FeedbackSummary
import com.kshitiz.messmate.domain.repository.FeedbackRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.Flow

class GetFeedbackSummaryUseCase(private val repository: FeedbackRepository) {
    operator fun invoke(mealType: String): Flow<Resource<FeedbackSummary>> {
        return repository.getFeedbackSummary(mealType)
    }
}
