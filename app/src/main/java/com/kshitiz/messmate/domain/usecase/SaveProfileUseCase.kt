package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.domain.repository.ProfileRepository
import com.kshitiz.messmate.util.Resource

class SaveProfileUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(user: User): Resource<Unit> {
        if (user.name.isBlank()) {
            return Resource.Error("Name cannot be empty")
        }
        return repository.saveProfile(user)
    }
}
