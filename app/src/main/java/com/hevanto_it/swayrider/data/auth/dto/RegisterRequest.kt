package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for a user registration request.
 * This class is used by Moshi to create the JSON body for the request.
 *
 * @property email The email address of the user to register.
 * @property password The desired password for the new account.
 * @property verificationUrl The URL that will be sent to the user for email verification.
 */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "verificationUrl") val verificationUrl: String
)