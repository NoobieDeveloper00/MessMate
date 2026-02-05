package com.kshitiz.messmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitiz.messmate.domain.model.DailyMenu
import com.kshitiz.messmate.domain.repository.AuthRepository
import com.kshitiz.messmate.domain.usecase.GetDailyMenuUseCase
import com.kshitiz.messmate.domain.usecase.GetUserAttendanceUseCase
import com.kshitiz.messmate.domain.usecase.OptOutMealUseCase
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

data class MenuUiState(
    val dailyMenu: Map<String, List<String>> = emptyMap(),
    val menuState: Resource<Map<String, List<String>>> = Resource.Loading,
    val optedOutMeals: Set<String> = emptySet(),
    val optOutState: Resource<String> = Resource.Idle
)

class MenuViewModel(
    private val authRepository: AuthRepository,
    private val getDailyMenuUseCase: GetDailyMenuUseCase,
    private val optOutMealUseCase: OptOutMealUseCase,
    private val getUserAttendanceUseCase: GetUserAttendanceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    // Keep these for backward compatibility if needed, or migration phase
    // but better to refactor the UI too. 
    // Actually, I'll refactor the UI to use uiState.
    
    // Legacy support for current UI
    val optOutState: StateFlow<Resource<String>> = _uiState.map { it.optOutState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Idle)
    val optedOutMeals: StateFlow<Set<String>> = _uiState.map { it.optedOutMeals }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    val dailyMenu: StateFlow<Map<String, List<String>>> = _uiState.map { it.dailyMenu }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        fetchDailyMenu()
        observeAttendance()
    }

    private fun observeAttendance() {
        val email = authRepository.getCurrentUser()?.email
        if (!email.isNullOrEmpty()) {
            getUserAttendanceUseCase(email)
                .onEach { resource ->
                    if (resource is Resource.Success) {
                        val attendance = resource.data
                        val optedOut = mutableSetOf<String>()
                        if (attendance?.breakfastOptOut == true) optedOut.add("breakfast")
                        if (attendance?.lunchOptOut == true) optedOut.add("lunch")
                        if (attendance?.snacksOptOut == true) optedOut.add("snacks")
                        if (attendance?.dinnerOptOut == true) optedOut.add("dinner")
                        _uiState.update { it.copy(optedOutMeals = optedOut) }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun fetchDailyMenu() {
        getDailyMenuUseCase()
            .onEach { resource ->
                when (resource) {
                    is Resource.Success -> {
                        val menuMap = resource.data?.toMap() ?: emptyMap()
                        _uiState.update { it.copy(
                            dailyMenu = menuMap,
                            menuState = Resource.Success(menuMap)
                        ) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(menuState = Resource.Error(resource.message)) }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(menuState = Resource.Loading) }
                    }
                    else -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    fun reloadMenu() {
        fetchDailyMenu()
    }

    fun optOutFromMeal(mealType: String) {
        val email = authRepository.getCurrentUser()?.email
        if (email.isNullOrEmpty()) {
            _uiState.update { it.copy(optOutState = Resource.Error("User not logged in")) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(optOutState = Resource.Loading) }
            val result = optOutMealUseCase(email, mealType)
            if (result is Resource.Success) {
                _uiState.update { it.copy(optOutState = Resource.Success("Opted out for $mealType")) }
            } else {
                _uiState.update { it.copy(optOutState = Resource.Error((result as Resource.Error).message)) }
            }
        }
    }
}