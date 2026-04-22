package com.hevanto_it.swayrider.domain.auth

import com.hevanto_it.swayrider.domain.user.UserProfile

/**
 * Represents the different states of user authentication within the application.
 *
 * This sealed class is used as the central state in the [AuthViewModel] to drive the UI.
 * Each state can optionally hold a [UserProfile].
 */
sealed class AuthState {
    /** The user profile associated with the state, which may be null. */
    abstract val profile: UserProfile?

    /**
     * The initial state while the application is determining the user's authentication status.
     * Typically shown on app startup.
     */
    object Loading : AuthState() {
        override val profile: UserProfile? = null
    }

    /**
     * The state representing a user who is not logged in.
     */
    object Unauthenticated : AuthState() {
        override val profile: UserProfile? = null
    }

    /**
     * The state representing a fully authenticated user whose email is verified.
     * @param profile The profile of the authenticated user.
     */
    data class Authenticated(
        override val profile: UserProfile?
    ) : AuthState()

    /**
     * The state representing a user who is logged in but has not yet verified their email.
     * @param profile The profile of the unverified user.
     */
    data class Unverified(
        override val profile: UserProfile?
    ) : AuthState()
}