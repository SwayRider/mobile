package com.hevanto_it.swayrider.data.auth.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer object for the response of the `whoAmI` request.
 * This class is used by Moshi to parse the JSON response from the server, containing the current user's profile information.
 *
 * @property userId The unique identifier of the authenticated user.
 * @property email The user's email address.
 * @property isVerified A boolean indicating if the user's email address has been verified.
 * @property isAdmin A boolean indicating if the user has administrative privileges.
 * @property accountType A string representing the type of the user's account (e.g., "standard", "premium").
 */
@JsonClass(generateAdapter = true)
data class WhoAmIResponse(
    @param:Json(name = "userId") val userId: String,
    @param:Json(name = "email") val email: String,
    @param:Json(name = "isVerified") val isVerified: Boolean,
    @param:Json(name = "isAdmin") val isAdmin: Boolean,
    @param:Json(name = "accountType") val accountType: String
)