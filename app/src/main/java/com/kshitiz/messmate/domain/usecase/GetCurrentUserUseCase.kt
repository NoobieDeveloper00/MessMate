package com.kshitiz.messmate.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.kshitiz.messmate.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): FirebaseUser? {
        return repository.getCurrentUser()
    }
}
