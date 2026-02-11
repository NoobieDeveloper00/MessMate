package com.kshitiz.messmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitiz.messmate.domain.model.DailyMenu
import com.kshitiz.messmate.domain.repository.MenuRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminMenuUiState(
    val menuState: Resource<Map<String, List<String>>> = Resource.Loading,
    val selectedDay: String = ""
)

class AdminMenuViewModel(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminMenuUiState(selectedDay = getCurrentDay()))
    val uiState: StateFlow<AdminMenuUiState> = _uiState.asStateFlow()

    init {
        loadMenu(_uiState.value.selectedDay)
    }

    fun selectDay(day: String) {
        _uiState.update { it.copy(selectedDay = day) }
        loadMenu(day)
    }

    private fun loadMenu(day: String) {
        menuRepository.getDailyMenu(day)
            .onEach { resource ->
                when (resource) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(menuState = Resource.Success(resource.data?.toMap() ?: emptyMap())) }
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

    fun addItem(mealType: String, item: String) {
        if (item.isBlank()) return
        val currentMenu = getCurrentDailyMenu() ?: DailyMenu()
        
        val updatedMenu = when (mealType) {
            "Breakfast" -> currentMenu.copy(breakfast = currentMenu.breakfast + item)
            "Lunch" -> currentMenu.copy(lunch = currentMenu.lunch + item)
            "Snacks" -> currentMenu.copy(snacks = currentMenu.snacks + item)
            "Dinner" -> currentMenu.copy(dinner = currentMenu.dinner + item)
            else -> currentMenu
        }
        
        saveMenu(updatedMenu)
    }

    fun removeItem(mealType: String, item: String) {
        val currentMenu = getCurrentDailyMenu() ?: return
        
        val updatedMenu = when (mealType) {
            "Breakfast" -> currentMenu.copy(breakfast = currentMenu.breakfast - item)
            "Lunch" -> currentMenu.copy(lunch = currentMenu.lunch - item)
            "Snacks" -> currentMenu.copy(snacks = currentMenu.snacks - item)
            "Dinner" -> currentMenu.copy(dinner = currentMenu.dinner - item)
            else -> currentMenu
        }
        
        saveMenu(updatedMenu)
    }

    private fun getCurrentDailyMenu(): DailyMenu? {
        val currentMap = (_uiState.value.menuState as? Resource.Success)?.data ?: return null
        return DailyMenu(
            breakfast = currentMap["Breakfast"] ?: emptyList(),
            lunch = currentMap["Lunch"] ?: emptyList(),
            snacks = currentMap["Snacks"] ?: emptyList(),
            dinner = currentMap["Dinner"] ?: emptyList()
        )
    }

    private fun saveMenu(menu: DailyMenu) {
        viewModelScope.launch {
            menuRepository.updateMenu(_uiState.value.selectedDay, menu)
        }
    }

    private fun getCurrentDay(): String {
        return SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
    }
}