package com.kshitiz.messmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitiz.messmate.domain.repository.AuthRepository
import com.kshitiz.messmate.domain.usecase.SubmitFeedbackUseCase
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FeedbackUiState(
    val submitState: Resource<String> = Resource.Idle
)

class FeedbackViewModel(
    private val authRepository: AuthRepository,
    private val submitFeedbackUseCase: SubmitFeedbackUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun submitFeedback(mealType: String, rating: Int, comment: String) {
        val email = authRepository.getCurrentUser()?.email ?: "Anonymous"
        
        viewModelScope.launch {
            _uiState.update { it.copy(submitState = Resource.Loading) }
            val result = submitFeedbackUseCase(mealType, rating, comment, email)
            if (result is Resource.Success) {
                _uiState.update { it.copy(submitState = Resource.Success("Feedback submitted successfully")) }
            } else {
                _uiState.update { it.copy(submitState = Resource.Error((result as Resource.Error).message)) }
            }
        }
    }
    
    fun resetState() {
        _uiState.update { it.copy(submitState = Resource.Idle) }
    }
}
