package com.kshitiz.messmate.domain.model

data class User(
    val uid: String,
    val name: String,
    val email: String,
    val favouriteMeal: String = "",
    val photoUrl: String = "",
    val role: String = "student"
)
