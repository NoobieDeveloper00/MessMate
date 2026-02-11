package com.kshitiz.messmate.domain.repository

import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.util.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun signup(name: String, email: String, password: String): Resource<User>
    suspend fun logout()
    fun getCurrentUser(): User?
    suspend fun isAdmin(email: String): Boolean
}
