package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.domain.repository.AuthRepository
import com.kshitiz.messmate.util.Resource

class SignupUseCase(private val repository: AuthRepository) {
    private val ALLOWED_DOMAIN = "iiitu.ac.in"
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    suspend operator fun invoke(name: String, email: String, password: String): Resource<User> {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            return Resource.Error("Please fill all fields")
        }

        if (!email.trim().endsWith("@$ALLOWED_DOMAIN", ignoreCase = true)) {
            return Resource.Error("Access Restricted: Only @$ALLOWED_DOMAIN emails are allowed.")
        }

        if (!EMAIL_REGEX.matches(email)) {
            return Resource.Error("Please enter a valid email address")
        }

        if (password.length < 6) {
            return Resource.Error("Password must be at least 6 characters")
        }

        return repository.signup(name, email, password)
    }
}
