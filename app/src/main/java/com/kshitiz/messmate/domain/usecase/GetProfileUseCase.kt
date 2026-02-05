package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.domain.repository.ProfileRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.Flow

class GetProfileUseCase(private val repository: ProfileRepository) {
    operator fun invoke(email: String): Flow<Resource<User>> {
        return repository.getProfile(email)
    }
}
