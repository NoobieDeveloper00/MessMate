package com.kshitiz.messmate.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.kshitiz.messmate.domain.repository.AuthRepository
import com.kshitiz.messmate.util.Resource

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Resource<FirebaseUser> {
        if (email.isBlank() || password.isBlank()) {
            return Resource.Error("Please fill all fields")
        }
        return repository.login(email, password)
    }
}
