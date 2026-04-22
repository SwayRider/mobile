package com.hevanto_it.swayrider.domain.user

/**
 * Represents the profile of a user within the application's domain layer.
 *
 * This data class holds user-specific information that is relevant to the UI and business logic,
 * abstracting it from the underlying data transfer objects.
 *
 * @property userId The unique identifier for the user.
 * @property email The user's email address.
 * @property isVerified A boolean indicating if the user's email has been verified.
 * @property isAdmin A boolean indicating if the user has administrative privileges.
 * @property accountType A string representing the type of the user's account.
 */
data class UserProfile (
    val userId: String,
    val email: String,
    val isVerified: Boolean,
    val isAdmin: Boolean,
    val accountType: String
)