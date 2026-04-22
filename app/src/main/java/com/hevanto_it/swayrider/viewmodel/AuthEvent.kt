package com.hevanto_it.swayrider.viewmodel

/**
 * Represents one-time events that can be sent from the [AuthViewModel] to the UI.
 *
 * This sealed class is used for events that should be consumed only once, such as showing
 * a Toast/Snackbar or triggering a navigation event that isn't tied to a permanent state change.
 */
sealed class AuthEvent {
    /**
     * An event to show an error message to the user.
     * @param message The error message to be displayed.
     */
    data class ShowError(val message: String) : AuthEvent()

    /**
     * An event indicating that the user has been successfully logged out.
     * This can be used to trigger navigation back to the login screen.
     */
    object LoggedOut : AuthEvent()

    /**
     * An event indicating that a password reset email has been sent.
     * Used to navigate to the confirmation screen.
     */
    object ForgotPasswordSent : AuthEvent()
}
