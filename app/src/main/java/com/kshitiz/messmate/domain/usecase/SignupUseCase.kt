package com.kshitiz.messmate.domain.usecase

import android.util.Patterns
import com.google.firebase.auth.FirebaseUser
import com.kshitiz.messmate.domain.repository.AuthRepository
import com.kshitiz.messmate.util.Resource

class SignupUseCase(private val repository: AuthRepository) {
    private val ALLOWED_DOMAIN = "iiitu.ac.in"

    suspend operator fun invoke(name: String, email: String, password: String): Resource<FirebaseUser> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Resource.Error("Please fill all fields")
        }

        if (!email.trim().endsWith("@$ALLOWED_DOMAIN", ignoreCase = true)) {
            return Resource.Error("Access Restricted: Only @$ALLOWED_DOMAIN emails are allowed.")
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Resource.Error("Please enter a valid email address")
        }

        if (password.length < 6) {
            return Resource.Error("Password must be at least 6 characters")
        }

        return repository.signup(name, email, password)
    }
}
