package com.kshitiz.messmate.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Initialize based on current user; never start as Loading
    private val _authState = MutableStateFlow<Resource<FirebaseUser>>(
        firebaseAuth.currentUser?.let { Resource.Success(it) } ?: Resource.Success(null)
    )
    val authState: StateFlow<Resource<FirebaseUser>> = _authState

    // Reset state to avoid re-triggering navigation on rotation
    fun resetState() {
        _authState.value = Resource.Idle
    }

    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Please fill all fields")
            return@launch
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = Resource.Error("Please enter a valid email address")
            return@launch
        }

        if (password.length < 6) {
            _authState.value = Resource.Error("Password must be at least 6 characters")
            return@launch
        }

        _authState.value = Resource.Loading
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            _authState.value = Resource.Success(result.user)
        } catch (e: Exception) {
            _authState.value = Resource.Error(mapSignupError(e))
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Please fill all fields")
            return@launch
        }

        _authState.value = Resource.Loading
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            _authState.value = Resource.Success(result.user)
        } catch (e: Exception) {
            _authState.value = Resource.Error(e.message ?: "Login failed")
        }
    }

    fun adminLogin(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Please fill all fields")
            return@launch
        }

        _authState.value = Resource.Loading
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            // Check Firestore admins whitelist
            val snapshot = firestore.collection("admins")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                _authState.value = Resource.Success(user)
            } else {
                // Not authorized: sign out and report error
                firebaseAuth.signOut()
                _authState.value = Resource.Error("Access Denied: You are not authorized as admin")
            }
        } catch (e: Exception) {
            _authState.value = Resource.Error(e.message ?: "Admin login failed")
        }
    }

    private fun mapSignupError(e: Exception): String {
        return when (e) {
            is FirebaseAuthUserCollisionException -> "This email is already registered"
            is FirebaseAuthInvalidCredentialsException -> "Invalid email address"
            is FirebaseNetworkException -> "Network error. Please check your connection and try again"
            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password sign-in is disabled for this project. Enable it in Firebase Console"
                "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters"
                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later"
                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please try again"
                else -> e.message ?: "Registration failed"
            }
            else -> e.message ?: "Registration failed"
        }
    }
}