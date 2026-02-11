package com.kshitiz.messmate.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.kshitiz.messmate.data.FirestoreConstants
import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.domain.repository.ProfileRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ProfileRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ProfileRepository {

    override fun getProfile(email: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading)
        val subscription = firestore.collection(FirestoreConstants.COLLECTION_USERS).document(email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching profile"))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = User(
                        uid = snapshot.getString("uid") ?: "",
                        name = snapshot.getString("name") ?: "",
                        email = snapshot.getString("email") ?: email,
                        favouriteMeal = snapshot.getString("favouriteMeal") ?: "",
                        photoUrl = snapshot.getString("photoUrl") ?: "",
                        role = snapshot.getString("role") ?: "student"
                    )
                    trySend(Resource.Success(user))
                } else {
                    trySend(Resource.Error("Profile not found"))
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveProfile(user: User): Resource<Unit> {
        return try {
            val data = mapOf(
                "name" to user.name,
                "favouriteMeal" to user.favouriteMeal
            )
            firestore.collection(FirestoreConstants.COLLECTION_USERS).document(user.email)
                .set(data, SetOptions.merge())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save profile")
        }
    }

    override suspend fun uploadProfilePicture(email: String, uri: Uri): Resource<String> {
        return try {
            val ref = storage.reference.child("profile_photos/${email.replace('/', '_')}.jpg")
            ref.putFile(uri).await()
            val downloadUri = ref.downloadUrl.await()
            val url = downloadUri.toString()
            
            firestore.collection(FirestoreConstants.COLLECTION_USERS).document(email)
                .set(mapOf("photoUrl" to url), SetOptions.merge())
                .await()
            
            Resource.Success(url)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to upload photo")
        }
    }
}
