package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.model.UserAttendance
import com.kshitiz.messmate.domain.repository.AttendanceRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetUserAttendanceUseCase(private val repository: AttendanceRepository) {
    operator fun invoke(email: String): Flow<Resource<UserAttendance>> {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return repository.getUserAttendance(email, dateString)
    }
}
