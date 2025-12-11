package com.kshitiz.messmate.ui.main.menu

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MenuViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _optOutState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val optOutState: StateFlow<Resource<String>> = _optOutState

    // Tracks which meals are opted out for today
    private val _optedOutMeals = MutableStateFlow<Set<String>>(emptySet())
    val optedOutMeals: StateFlow<Set<String>> = _optedOutMeals

    init {
        // Listen for today's opt-out changes so UI can reflect status
        val email = auth.currentUser?.email
        if (!email.isNullOrEmpty()) {
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            firestore.collection("users")
                .document(email)
                .collection("attendance")
                .document(dateString)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        val set = buildSet {
                            if (snapshot.getBoolean("breakfast_optout") == true) add("breakfast")
                            if (snapshot.getBoolean("lunch_optout") == true) add("lunch")
                            if (snapshot.getBoolean("snacks_optout") == true) add("snacks")
                            if (snapshot.getBoolean("dinner_optout") == true) add("dinner")
                        }
                        _optedOutMeals.value = set
                    }
                }
        }
    }

    fun optOutFromMeal(mealType: String) {
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            _optOutState.value = Resource.Error("User not logged in")
            return
        }

        val mealKey = mealType.lowercase(Locale.getDefault())
        // Too late if current time is at or past cutoff
        if (!isBeforeCutoff(mealKey)) {
            _optOutState.value = Resource.Error("Too late to opt out for this meal")
            return
        }

        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val optOutKey = "${mealKey}_optout"

        val docRef = firestore.collection("users")
            .document(email)
            .collection("attendance")
            .document(dateString)

        val data = mapOf(optOutKey to true)

        docRef.set(data, SetOptions.merge())
            .addOnSuccessListener {
                _optOutState.value = Resource.Success("Opted out for ${mealType}")
            }
            .addOnFailureListener { e ->
                _optOutState.value = Resource.Error(e.message ?: "Failed to opt out", e)
            }
    }

    private fun isBeforeCutoff(mealKey: String): Boolean {
        val now = java.util.Calendar.getInstance()
        val nowMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
        val cutoffMinutes = when (mealKey) {
            "breakfast" -> 7 * 60 + 0
            "lunch" -> 10 * 60 + 0
            "snacks" -> 16 * 60 + 0
            "dinner" -> 19 * 60 + 0
            else -> 23 * 60 + 59
        }
        return nowMinutes < cutoffMinutes
    }
}
