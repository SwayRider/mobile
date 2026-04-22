package com.hevanto_it.swayrider.domain.auth

/**
 * Represents the state of a password strength check operation.
 *
 * This sealed class is used to reflect the UI state related to real-time password validation,
 * for example, in a registration or change password screen.
 */
sealed class PasswordStrengthState {
    /** The password check is not active. This is the initial or reset state. */
    object Idle : PasswordStrengthState()

    /** The password check is currently in progress (e.g., waiting for a network response). */
    object Checking : PasswordStrengthState()

    /** The password has been validated and meets the strength requirements. */
    object Strong : PasswordStrengthState()

    /** The password has been validated and does not meet the strength requirements. */
    object Weak : PasswordStrengthState()
}