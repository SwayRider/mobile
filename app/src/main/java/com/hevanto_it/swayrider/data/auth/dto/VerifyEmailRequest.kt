package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for an email verification request.
 * This class is used by Moshi to create the JSON body for the request.
 *
 * @property email The email address to send the verification link to.
 * @property verificationUrl The URL that the user will be directed to from the verification email.
 */
@JsonClass(generateAdapter = true)
data class VerifyEmailRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "verificationUrl") val verificationUrl: String
)