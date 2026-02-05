package com.kshitiz.messmate.domain.repository

import android.net.Uri
import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(email: String): Flow<Resource<User>>
    suspend fun saveProfile(user: User): Resource<Unit>
    suspend fun uploadProfilePicture(email: String, uri: Uri): Resource<String>
}
