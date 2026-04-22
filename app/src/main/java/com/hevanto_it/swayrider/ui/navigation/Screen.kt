package com.hevanto_it.swayrider.ui.navigation

/**
 * A sealed class representing all possible navigation destinations in the application.
 *
 * Using a sealed class for navigation routes provides type safety and ensures that all
 * navigation paths are explicitly defined. Each object represents a unique screen.
 *
 * @property route The unique string identifier for the screen, used in the navigation graph.
 */
sealed class Screen(val route: String) {
    /** The login screen, where users authenticate. */
    object Login : Screen("login")

    /** The registration screen, for creating new accounts. */
    object Register : Screen("register")

    /** A state within the home screen for users who have not yet verified their email. Not a direct destination. */
    object Unverified : Screen("unverified")

    /** The main home screen, the landing page for authenticated users. */
    object Home : Screen("home")

    /** The screen for planning a new route. */
    object RoutePlanning : Screen("route_planning")

    /** The screen where users enter their email to request a password reset. */
    object ForgotPassword : Screen("forgot_password")

    /** The confirmation screen shown after a password reset email is sent. */
    object ForgotPasswordConfirmation : Screen("forgot_password_confirmation")
}