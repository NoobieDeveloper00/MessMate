package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.repository.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
