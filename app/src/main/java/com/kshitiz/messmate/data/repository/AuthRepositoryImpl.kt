package com.kshitiz.messmate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kshitiz.messmate.domain.repository.AuthRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Resource.Success(result.user!!)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun signup(name: String, email: String, password: String): Resource<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            
            val userProfile = hashMapOf(
                "uid" to user.uid,
                "name" to name,
                "email" to email,
                "favouriteMeal" to "",
                "photoUrl" to "",
                "role" to "student"
            )
            firestore.collection("users").document(email).set(userProfile).await()
            
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Signup failed")
        }
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    override suspend fun isAdmin(email: String): Boolean {
        return try {
            val snapshot = firestore.collection("admins")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}
