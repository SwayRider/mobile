package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for a user login request.
 * This class is used by Moshi to create the JSON body for the request.
 *
 * @property email The user's email address.
 * @property password The user's password.
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String
)
