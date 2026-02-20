package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.repository.FeedbackRepository
import com.kshitiz.messmate.util.Resource

class DeleteOldFeedbackUseCase(private val repository: FeedbackRepository) {
    suspend operator fun invoke(): Resource<Unit> {
        return repository.deleteOldFeedback()
    }
}
