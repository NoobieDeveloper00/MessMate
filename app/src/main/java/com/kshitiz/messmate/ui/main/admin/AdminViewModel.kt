package com.kshitiz.messmate.ui.main.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminViewModel(private val firestore: FirebaseFirestore) : ViewModel() {

    private val _scanState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val scanState: StateFlow<Resource<String>> = _scanState

    fun resetState() {
        _scanState.value = Resource.Idle
    }

    fun markAttendance(studentEmail: String, mealType: String) {
        viewModelScope.launch {
            try {
                _scanState.value = Resource.Loading

                val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val mealKey = mealType.lowercase(Locale.getDefault())
                val optOutKey = "${mealKey}_optout"

                val docRef = firestore.collection("users")
                    .document(studentEmail)
                    .collection("attendance")
                    .document(dateString)

                // First read to prevent duplicates and respect opt-out
                docRef.get()
                    .addOnSuccessListener { snapshot ->
                        val alreadyMarked = snapshot.getBoolean(mealKey) == true
                        if (alreadyMarked) {
                            _scanState.value = Resource.Error("Attendance already marked for this meal")
                            return@addOnSuccessListener
                        }

                        val optedOut = snapshot.getBoolean(optOutKey) == true
                        if (optedOut) {
                            _scanState.value = Resource.Error("Student has opted out. Entry Denied.")
                            return@addOnSuccessListener
                        }

                        val data = mapOf(
                            mealKey to true
                        )

                        docRef.set(data, SetOptions.merge())
                            .addOnSuccessListener {
                                _scanState.value = Resource.Success("Marked for ${mealType}")
                            }
                            .addOnFailureListener { e ->
                                _scanState.value = Resource.Error(e.message ?: "Failed to mark attendance", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        _scanState.value = Resource.Error(e.message ?: "Failed to check attendance", e)
                    }
            } catch (e: Exception) {
                _scanState.value = Resource.Error(e.message ?: "Failed to mark attendance", e)
            }
        }
    }
}
