package com.kshitiz.messmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitiz.messmate.domain.model.FeedbackSummary
import com.kshitiz.messmate.domain.usecase.GetFeedbackSummaryUseCase
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.*

data class AdminFeedbackUiState(
    val summaryState: Resource<FeedbackSummary> = Resource.Loading
)

class AdminFeedbackViewModel(
    private val getFeedbackSummaryUseCase: GetFeedbackSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFeedbackUiState())
    val uiState: StateFlow<AdminFeedbackUiState> = _uiState.asStateFlow()

    // Legacy support
    val summaryState: StateFlow<Resource<FeedbackSummary>> = _uiState.map { it.summaryState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    init {
        loadFeedback("Breakfast")
    }

    fun loadFeedback(mealType: String) {
        getFeedbackSummaryUseCase(mealType)
            .onEach { resource ->
                _uiState.update { it.copy(summaryState = resource) }
            }
            .launchIn(viewModelScope)
    }
}