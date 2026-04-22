package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Represents a structured error response from the API.
 * This class is used by Moshi to parse JSON error bodies.
 *
 * @property code The specific error code returned by the API.
 * @property message A human-readable error message.
 * @property details An optional list of more detailed error messages or validation failures.
 */
@JsonClass(generateAdapter = true)
data class ApiError(
    @param:Json(name = "code") val code: Int,
    @param:Json(name = "message") val message: String,
    @param:Json(name = "details") val details: List<String>?
)
