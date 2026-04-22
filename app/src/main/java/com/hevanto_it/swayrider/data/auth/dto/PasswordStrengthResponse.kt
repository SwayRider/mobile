package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for the response of a password strength check.
 * This class is used by Moshi to parse the JSON response from the server.
 *
 * @property isStrong Indicates whether the password meets the strength requirements.
 * @property message A descriptive message about the password's strength (e.g., "Password is too short").
 */
@JsonClass(generateAdapter = true)
data class PasswordStrengthResponse (
    @param:Json(name = "isStrong") val isStrong: Boolean,
    @param:Json(name = "message") val message: String
)