package com.kshitiz.messmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitiz.messmate.domain.usecase.MarkAttendanceUseCase
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AdminUiState(
    val scanState: Resource<String> = Resource.Idle
)

class AdminViewModel(
    private val markAttendanceUseCase: MarkAttendanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    // Legacy support
    val scanState: StateFlow<Resource<String>> = _uiState.map { it.scanState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Idle)

    fun resetState() {
        _uiState.update { it.copy(scanState = Resource.Idle) }
    }

    fun markAttendance(studentEmail: String, mealType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(scanState = Resource.Loading) }
            val result = markAttendanceUseCase(studentEmail, mealType)
            if (result is Resource.Success) {
                _uiState.update { it.copy(scanState = Resource.Success("Marked for $mealType")) }
            } else {
                _uiState.update { it.copy(scanState = Resource.Error((result as Resource.Error).message)) }
            }
        }
    }
}
