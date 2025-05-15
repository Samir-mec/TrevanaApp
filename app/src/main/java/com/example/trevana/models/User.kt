package com.example.trevana.models

import java.util.Date

data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val phone: String? = null,
    val profilePicture: String? = null,
    val joinDate: Date? = null
)