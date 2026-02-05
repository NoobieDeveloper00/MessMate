package com.kshitiz.messmate.domain.model

data class DailyMenu(
    val breakfast: List<String> = emptyList(),
    val lunch: List<String> = emptyList(),
    val snacks: List<String> = emptyList(),
    val dinner: List<String> = emptyList()
) {
    fun toMap(): Map<String, List<String>> {
        return mapOf(
            "Breakfast" to breakfast,
            "Lunch" to lunch,
            "Snacks" to snacks,
            "Dinner" to dinner
        )
    }
}
