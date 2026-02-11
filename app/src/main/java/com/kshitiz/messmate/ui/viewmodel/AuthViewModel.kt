package com.kshitiz.messmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitiz.messmate.domain.model.User
import com.kshitiz.messmate.domain.usecase.*
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val authState: Resource<User> = Resource.Idle
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val isAdminUseCase: IsAdminUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AuthUiState(authState = getCurrentUserUseCase()?.let { Resource.Success(it) } ?: Resource.Idle)
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun getCurrentUser(): User? = getCurrentUserUseCase()

    fun isLoggedIn(): Boolean = getCurrentUserUseCase() != null

    fun resetState() {
        _uiState.update { it.copy(authState = Resource.Idle) }
    }

    fun signup(name: String, email: String, password: String) = viewModelScope.launch {
        _uiState.update { it.copy(authState = Resource.Loading) }
        _uiState.update { it.copy(authState = signupUseCase(name, email, password)) }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _uiState.update { it.copy(authState = Resource.Loading) }
        _uiState.update { it.copy(authState = loginUseCase(email, password)) }
    }

    fun logout() = viewModelScope.launch {
        logoutUseCase()
        _uiState.update { it.copy(authState = Resource.Idle) }
    }

    fun adminLogin(email: String, password: String) = viewModelScope.launch {
        _uiState.update { it.copy(authState = Resource.Loading) }
        val loginResult = loginUseCase(email, password)
        if (loginResult is Resource.Success) {
            val isAdmin = isAdminUseCase(email)
            if (isAdmin) {
                _uiState.update { it.copy(authState = loginResult) }
            } else {
                logoutUseCase()
                _uiState.update { it.copy(authState = Resource.Error("Access Denied: You are not authorized as admin")) }
            }
        } else {
            _uiState.update { it.copy(authState = loginResult) }
        }
    }
}