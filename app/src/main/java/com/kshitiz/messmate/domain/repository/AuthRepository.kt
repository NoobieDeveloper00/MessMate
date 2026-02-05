package com.kshitiz.messmate.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.kshitiz.messmate.util.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<FirebaseUser>
    suspend fun signup(name: String, email: String, password: String): Resource<FirebaseUser>
    suspend fun logout()
    fun getCurrentUser(): FirebaseUser?
    suspend fun isAdmin(email: String): Boolean
}
