package com.kshitiz.messmate.domain.repository

import com.kshitiz.messmate.domain.model.UserAttendance
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getUserAttendance(email: String, date: String): Flow<Resource<UserAttendance>>
    suspend fun markAttendance(email: String, date: String, mealType: String): Resource<Unit>
    suspend fun optOutMeal(email: String, date: String, mealType: String): Resource<Unit>
}
