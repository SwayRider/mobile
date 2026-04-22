package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for the response of a user registration request.
 * This class is used by Moshi to parse the JSON response from the server.
 *
 * @property userId The unique identifier for the newly created user.
 * @property message A confirmation message from the server.
 */
@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @param:Json(name = "userId") val userId: String,
    @param:Json(name = "message") val message: String
)