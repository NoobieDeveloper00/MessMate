package com.kshitiz.messmate.domain.repository

import com.kshitiz.messmate.domain.model.DailyMenu
import com.kshitiz.messmate.util.Resource
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun getDailyMenu(day: String): Flow<Resource<DailyMenu>>
    suspend fun updateMenu(day: String, menu: DailyMenu): Resource<Unit>
}
