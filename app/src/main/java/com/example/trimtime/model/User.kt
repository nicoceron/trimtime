package com.example.trimtime.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    var id: Int = 0,
    var firstName: String = "",
    var lastName: String = "",
    var age: Int = 0,
    var appointmentTime: String = ""
) {
    // Firebase requires a no-argument constructor
    constructor() : this(0, "", "", 0, "")
}