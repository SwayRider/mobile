package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for the response of a login or token refresh request.
 * This class is used by Moshi to parse the JSON response from the server.
 *
 * @property accessToken The JSON Web Token (JWT) used to authenticate API requests.
 * @property refreshToken The token used to obtain a new access token when the current one expires.
 */
@JsonClass(generateAdapter = true)
data class TokenResponse(
    @param:Json(name = "accessToken") val accessToken: String,
    @param:Json(name = "refreshToken") val refreshToken: String
)