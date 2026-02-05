package com.kshitiz.messmate.domain.model

data class UserAttendance(
    val date: String,
    val breakfast: Boolean = false,
    val lunch: Boolean = false,
    val snacks: Boolean = false,
    val dinner: Boolean = false,
    val breakfastOptOut: Boolean = false,
    val lunchOptOut: Boolean = false,
    val snacksOptOut: Boolean = false,
    val dinnerOptOut: Boolean = false
)
