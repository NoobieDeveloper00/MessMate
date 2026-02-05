package com.kshitiz.messmate.domain.usecase

import com.kshitiz.messmate.domain.model.DailyMenu
import com.kshitiz.messmate.domain.repository.MenuRepository
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GetDailyMenuUseCase(private val repository: MenuRepository) {
    operator fun invoke(): Flow<Resource<DailyMenu>> {
        val dayOfWeek = SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date())
        return repository.getDailyMenu(dayOfWeek)
    }
}
