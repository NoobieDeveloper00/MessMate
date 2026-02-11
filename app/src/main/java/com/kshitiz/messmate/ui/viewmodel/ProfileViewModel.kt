package com.kshitiz.messmate.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.domain.repository.AuthRepository
import com.kshitiz.messmate.domain.repository.ProfileRepository
import com.kshitiz.messmate.domain.usecase.GetProfileUseCase
import com.kshitiz.messmate.domain.usecase.SaveProfileUseCase
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profileState: Resource<User> = Resource.Loading
)

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val getProfileUseCase: GetProfileUseCase,
    private val saveProfileUseCase: SaveProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val email = authRepository.getCurrentUser()?.email
        if (email.isNullOrEmpty()) {
            _uiState.update { it.copy(profileState = Resource.Error("User not logged in")) }
            return
        }

        getProfileUseCase(email)
            .onEach { resource ->
                _uiState.update { it.copy(profileState = resource) }
            }
            .launchIn(viewModelScope)
    }

    fun saveProfile(name: String, favouriteMeal: String) {
        val currentUser = (_uiState.value.profileState as? Resource.Success)?.data
        if (currentUser == null) {
            _uiState.update { it.copy(profileState = Resource.Error("Profile data not loaded")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(profileState = Resource.Loading) }
            val updatedUser = currentUser.copy(name = name, favouriteMeal = favouriteMeal)
            val result = saveProfileUseCase(updatedUser)
            if (result is Resource.Error) {
                _uiState.update { it.copy(profileState = Resource.Error(result.message)) }
            }
            // Success will be handled by the flow from getProfileUseCase if it's observing firestore
        }
    }

    fun uploadPhoto(uri: Uri) {
        val email = authRepository.getCurrentUser()?.email
        if (email.isNullOrEmpty()) {
            _uiState.update { it.copy(profileState = Resource.Error("User not logged in")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(profileState = Resource.Loading) }
            val result = profileRepository.uploadProfilePicture(email, uri)
            if (result is Resource.Error) {
                _uiState.update { it.copy(profileState = Resource.Error(result.message)) }
            }
        }
    }
}
