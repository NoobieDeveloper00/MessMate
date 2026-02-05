package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.repository.AuthRepository

class IsAdminUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): Boolean {
        return repository.isAdmin(email)
    }
}
