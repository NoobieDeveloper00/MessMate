package com.kshitiz.messmate.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kshitiz.messmate.data.FirestoreConstants
import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.domain.repository.AuthRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!
            Resource.Success(
                User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: email
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    override suspend fun signup(name: String, email: String, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!
            
            val userProfile = hashMapOf(
                "uid" to firebaseUser.uid,
                "name" to name,
                "email" to email,
                "favouriteMeal" to "",
                "photoUrl" to "",
                "role" to "student"
            )
            firestore.collection(FirestoreConstants.COLLECTION_USERS).document(email).set(userProfile).await()
            
            Resource.Success(
                User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Signup failed")
        }
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        firebaseAuth.signOut()
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
    }

    override suspend fun isAdmin(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(FirestoreConstants.COLLECTION_ADMINS)
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
