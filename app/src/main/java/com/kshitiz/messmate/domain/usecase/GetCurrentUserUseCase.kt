package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): User? {
        return repository.getCurrentUser()
    }
}
