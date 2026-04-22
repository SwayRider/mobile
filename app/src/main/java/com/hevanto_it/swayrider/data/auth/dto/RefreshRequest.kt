package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for a token refresh request.
 * This class is used by Moshi to create the JSON body for the request.
 *
 * @property refreshToken The refresh token used to obtain a new access token.
 */
@JsonClass(generateAdapter = true)
data class RefreshRequest(
    @param:Json(name = "refreshToken") val refreshToken: String
)
