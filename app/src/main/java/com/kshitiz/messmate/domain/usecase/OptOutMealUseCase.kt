package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.repository.AttendanceRepository
import com.kshitiz.messmate.util.Resource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OptOutMealUseCase(private val repository: AttendanceRepository) {
    suspend operator fun invoke(email: String, mealType: String): Resource<Unit> {
        val mealKey = mealType.lowercase(Locale.getDefault())
        if (!isBeforeCutoff(mealKey)) {
            return Resource.Error("Too late to opt out for this meal")
        }

        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return repository.optOutMeal(email, dateString, mealType)
    }

    private fun isBeforeCutoff(mealKey: String): Boolean {
        val now = Calendar.getInstance()
        val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val cutoffMinutes = when (mealKey) {
            "breakfast" -> 7 * 60 + 0
            "lunch" -> 12 * 60 + 0
            "snacks" -> 16 * 60 + 0
            "dinner" -> 19 * 60 + 0
            else -> 23 * 60 + 59
        }
        return nowMinutes < cutoffMinutes
    }
}
