package com.kshitiz.messmate.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val favouriteMeal: String = "",
    val photoUrl: String = ""
)

class ProfileViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<UserProfile>>(Resource.Loading)
    val profileState: StateFlow<Resource<UserProfile>> = _profileState

    init {
        loadProfile()
    }

    fun loadProfile() {
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            _profileState.value = Resource.Error("User not logged in")
            return
        }

        firestore.collection("users").document(email)
            .get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: auth.currentUser?.displayName.orEmpty()
                val fav = doc.getString("favouriteMeal") ?: ""
                val photo = doc.getString("photoUrl") ?: ""
                _profileState.value = Resource.Success(
                    UserProfile(
                        name = name,
                        email = email,
                        favouriteMeal = fav,
                        photoUrl = photo
                    )
                )
            }
            .addOnFailureListener { e ->
                _profileState.value = Resource.Error(e.message ?: "Failed to load profile", e)
            }
    }

    fun saveProfile(name: String, favouriteMeal: String) {
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            _profileState.value = Resource.Error("User not logged in")
            return
        }
        val data = mapOf(
            "name" to name,
            "favouriteMeal" to favouriteMeal
        )
        firestore.collection("users").document(email)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                // Refresh state
                val current = (_profileState.value as? Resource.Success)?.data
                _profileState.value = Resource.Success(
                    (current ?: UserProfile(email = email)).copy(name = name, favouriteMeal = favouriteMeal)
                )
            }
            .addOnFailureListener { e ->
                _profileState.value = Resource.Error(e.message ?: "Failed to save profile", e)
            }
    }

    fun uploadPhoto(uri: Uri) {
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            _profileState.value = Resource.Error("User not logged in")
            return
        }
        val ref = storage.reference.child("profile_photos/${email.replace('/', '_')}.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    val url = downloadUri.toString()
                    firestore.collection("users").document(email)
                        .set(mapOf("photoUrl" to url), SetOptions.merge())
                        .addOnSuccessListener {
                            val current = (_profileState.value as? Resource.Success)?.data
                            _profileState.value = Resource.Success(
                                (current ?: UserProfile(email = email)).copy(photoUrl = url)
                            )
                        }
                        .addOnFailureListener { e ->
                            _profileState.value = Resource.Error(e.message ?: "Failed to save photo url", e)
                        }
                }.addOnFailureListener { e ->
                    _profileState.value = Resource.Error(e.message ?: "Failed to get photo url", e)
                }
            }
            .addOnFailureListener { e ->
                _profileState.value = Resource.Error(e.message ?: "Failed to upload photo", e)
            }
    }
}
