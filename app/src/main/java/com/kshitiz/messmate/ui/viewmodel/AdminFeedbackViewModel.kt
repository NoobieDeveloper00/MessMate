package com.kshitiz.messmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitiz.messmate.domain.model.FeedbackSummary
import com.kshitiz.messmate.domain.usecase.DeleteOldFeedbackUseCase
import com.kshitiz.messmate.domain.usecase.GetFeedbackSummaryUseCase
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class AdminFeedbackUiState(
    val summaryState: Resource<FeedbackSummary> = Resource.Loading,
    val selectedDate: String = "",
    val selectedMeal: String = "Breakfast",
    val availableDates: List<Pair<String, String>> = emptyList() // (date string, display label)
)

class AdminFeedbackViewModel(
    private val getFeedbackSummaryUseCase: GetFeedbackSummaryUseCase,
    private val deleteOldFeedbackUseCase: DeleteOldFeedbackUseCase
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.US)

    private val _uiState = MutableStateFlow(AdminFeedbackUiState())
    val uiState: StateFlow<AdminFeedbackUiState> = _uiState.asStateFlow()

    init {
        val dates = buildAvailableDates()
        val today = dates.first().first
        _uiState.update { it.copy(availableDates = dates, selectedDate = today) }

        // Clean up old feedback on launch
        viewModelScope.launch {
            deleteOldFeedbackUseCase()
        }

        loadFeedback("Breakfast", today)
    }

    private fun buildAvailableDates(): List<Pair<String, String>> {
        val dates = mutableListOf<Pair<String, String>>()
        val calendar = Calendar.getInstance()
        for (i in 0 until 8) {
            val dateStr = dateFormat.format(calendar.time)
            val label = when (i) {
                0 -> "Today"
                1 -> "Yesterday"
                else -> displayFormat.format(calendar.time)
            }
            dates.add(dateStr to label)
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return dates
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
        loadFeedback(_uiState.value.selectedMeal, date)
    }

    fun selectMeal(meal: String) {
        _uiState.update { it.copy(selectedMeal = meal) }
        loadFeedback(meal, _uiState.value.selectedDate)
    }

    fun loadFeedback(mealType: String, date: String) {
        getFeedbackSummaryUseCase(mealType, date)
            .onEach { resource ->
                _uiState.update { it.copy(summaryState = resource) }
            }
            .launchIn(viewModelScope)
    }
}