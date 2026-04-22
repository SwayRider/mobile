package com.hevanto_it.swayrider.domain.auth

import com.hevanto_it.swayrider.core.network.AuthRequired
import com.hevanto_it.swayrider.core.network.NetworkResult
import com.hevanto_it.swayrider.data.auth.dto.WhoAmIResponse
import com.hevanto_it.swayrider.domain.user.UserProfile

/**
 * Interface defining the contract for authentication-related operations.
 * This includes registration, login, token refreshing, and fetching user information.
 */
interface AuthService {
    /**
     * Checks the strength of a given password.
     * @param password The password to check.
     * @return A [NetworkResult] containing the [PasswordStrength].
     */
    suspend fun checkPasswordStrength(password: String): NetworkResult<PasswordStrength>

    /**
     * Registers a new user.
     * @param email The user's email.
     * @param password The user's password.
     * @param verificationUrl The URL for email verification.
     * @return A [NetworkResult] containing the [RegistrationResult].
     */
    suspend fun register(email: String, password: String, verificationUrl: String): NetworkResult<RegistrationResult>

    /**
     * Logs in a user.
     * @param email The user's email.
     * @param password The user's password.
     * @return A [NetworkResult] containing the [TokenPair].
     */
    suspend fun login(email: String, password: String): NetworkResult<TokenPair>

    /**
     * Refreshes the authentication tokens.
     * @param refreshToken The refresh token.
     * @return A [NetworkResult] containing a new [TokenPair].
     */
    suspend fun refresh(refreshToken: String): NetworkResult<TokenPair>

    /**
     * Fetches the current user's profile information. Requires authentication.
     * @return A [NetworkResult] containing the [WhoAmI] data.
     */
    @AuthRequired
    suspend fun whoAmI(): NetworkResult<WhoAmI>

    /**
     * Requests a new verification email to be sent.
     * @param email The user's email.
     * @param verificationUrl The URL for email verification.
     * @return A [NetworkResult] indicating the outcome.
     */
    suspend fun verifyEmail(email: String, verificationUrl: String): NetworkResult<Unit>

    /**
     * Requests a password reset email to be sent.
     * Always returns success to prevent email enumeration.
     * @param email The user's email.
     * @param resetUrl The URL embedded in the reset email link.
     * @return A [NetworkResult] indicating the outcome.
     */
    suspend fun forgotPassword(email: String, resetUrl: String): NetworkResult<Unit>
}

/** Data class representing a pair of authentication tokens. */
data class TokenPair(
    val jwt: String,
    val refresh: String
)

/** Data class representing the result of a password strength check. */
data class PasswordStrength(
    val isStrong: Boolean,
    val message: String
)

/** Data class representing the result of a user registration. */
data class RegistrationResult(
    val userId: String,
    val message: String
)

/** Data class representing the current user's identity and status. */
data class WhoAmI(
    val userId: String,
    val email: String,
    val isVerified: Boolean,
    val isAdmin: Boolean,
    val accountType: String
)

/** Extension function to convert a [WhoAmI] data object to a [UserProfile] domain model. */
fun WhoAmI.toDomain(): UserProfile =
    UserProfile(
        userId = userId,
        email = email,
        isVerified = isVerified,
        isAdmin = isAdmin,
        accountType
    )
