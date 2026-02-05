package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.repository.AttendanceRepository
import com.kshitiz.messmate.util.Resource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarkAttendanceUseCase(private val repository: AttendanceRepository) {
    suspend operator fun invoke(email: String, mealType: String): Resource<Unit> {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return repository.markAttendance(email, dateString, mealType)
    }
}
