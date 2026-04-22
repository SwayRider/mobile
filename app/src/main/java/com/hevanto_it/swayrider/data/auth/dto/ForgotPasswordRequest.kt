package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "resetUrl") val resetUrl: String
)
