package com.example.trimtime.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val appointmentTime: String
)
