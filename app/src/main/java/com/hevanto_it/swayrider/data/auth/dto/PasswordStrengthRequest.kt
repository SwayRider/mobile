package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for a password strength check request.
 * This class is used by Moshi to create the JSON body for the request.
 *
 * @property password The password to be checked.
 */
@JsonClass(generateAdapter = true)
data class PasswordStrengthRequest (
    @param:Json(name = "password") val password: String
)