package com.kshitiz.messmate.ui.main.admin.menu

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class AdminMenuViewModel(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _menuState = MutableStateFlow<Resource<Map<String, List<String>>>>(Resource.Loading)
    val menuState: StateFlow<Resource<Map<String, List<String>>>> = _menuState

    private val _selectedDay = MutableStateFlow(getCurrentDay())
    val selectedDay: StateFlow<String> = _selectedDay

    init {
        loadMenu(_selectedDay.value)
    }

    fun selectDay(day: String) {
        _selectedDay.value = day
        loadMenu(day)
    }

    private fun loadMenu(day: String) {
        _menuState.value = Resource.Loading
        firestore.collection("menus").document(day)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val menuMap = mutableMapOf<String, List<String>>()
                    // Map Firestore keys (lowercase) to UI keys (Capitalized)
                    menuMap["Breakfast"] = document.get("breakfast") as? List<String> ?: emptyList()
                    menuMap["Lunch"] = document.get("lunch") as? List<String> ?: emptyList()
                    menuMap["Snacks"] = document.get("snacks") as? List<String> ?: emptyList()
                    menuMap["Dinner"] = document.get("dinner") as? List<String> ?: emptyList()
                    _menuState.value = Resource.Success(menuMap)
                } else {
                    // Empty menu for this day
                    _menuState.value = Resource.Success(emptyMap())
                }
            }
            .addOnFailureListener { e ->
                _menuState.value = Resource.Error(e.message ?: "Failed to load menu", e)
            }
    }

    fun addItem(mealType: String, item: String) {
        if (item.isBlank()) return

        val currentMap = (_menuState.value as? Resource.Success)?.data ?: return
        val currentList = currentMap[mealType] ?: emptyList()

        // Optimistic Update
        val newList = currentList + item
        updateFirestore(mealType, newList)
    }

    fun removeItem(mealType: String, item: String) {
        val currentMap = (_menuState.value as? Resource.Success)?.data ?: return
        val currentList = currentMap[mealType] ?: emptyList()

        val newList = currentList - item
        updateFirestore(mealType, newList)
    }

    private fun updateFirestore(mealType: String, newList: List<String>) {
        val day = _selectedDay.value
        val fieldName = mealType.lowercase(Locale.ENGLISH) // "Breakfast" -> "breakfast"

        firestore.collection("menus").document(day)
            .set(mapOf(fieldName to newList), SetOptions.merge())
            .addOnSuccessListener {
                loadMenu(day) // Reload to sync state perfectly
            }
    }

    private fun getCurrentDay(): String {
        return LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    }
}