package com.hevanto_it.swayrider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hevanto_it.swayrider.domain.auth.AuthState
import com.hevanto_it.swayrider.ui.components.AppScaffold
import com.hevanto_it.swayrider.ui.navigation.Screen
import com.hevanto_it.swayrider.ui.screens.ForgotPasswordConfirmationScreen
import com.hevanto_it.swayrider.ui.screens.ForgotPasswordScreen
import com.hevanto_it.swayrider.ui.screens.HomeScreen
import com.hevanto_it.swayrider.ui.screens.LoginScreen
import com.hevanto_it.swayrider.ui.screens.RegistrationScreen
import com.hevanto_it.swayrider.ui.screens.RoutePlanningScreen
import com.hevanto_it.swayrider.ui.screens.UnverifiedScreen
import com.hevanto_it.swayrider.viewmodel.AuthViewModel
import com.hevanto_it.swayrider.viewmodel.LocationSearchViewModel

/**
 * The main navigation host for the SwayRider application.
 */
@Composable
fun SwayRiderNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    authState: AuthState,
    locationSearchViewModel: LocationSearchViewModel
) {
    // Determine the appropriate starting screen based on the initial authentication state.
    // We stay on Login if we are Loading or Unauthenticated.
    val startDestination = when(authState) {
        is AuthState.Authenticated, is AuthState.Unverified -> Screen.Home.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // --- Public Routes --- //

        composable(Screen.Login.route) {
            // Redirect only if we are definitively NOT Unauthenticated and NOT Loading.
            if (authState is AuthState.Authenticated || authState is AuthState.Unverified) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            } else if (authState is AuthState.Unauthenticated) {
                LoginScreen(navController, authViewModel)
            }
            // If Loading, we just wait (SwayRiderApp shows a progress indicator).
        }

        composable(Screen.Register.route) {
            if (authState is AuthState.Authenticated || authState is AuthState.Unverified) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            } else if (authState is AuthState.Unauthenticated) {
                RegistrationScreen(navController, authViewModel)
            }
        }

        composable(Screen.ForgotPassword.route) {
            if (authState is AuthState.Unauthenticated) {
                ForgotPasswordScreen(navController, authViewModel)
            }
        }

        composable(Screen.ForgotPasswordConfirmation.route) {
            if (authState is AuthState.Unauthenticated) {
                ForgotPasswordConfirmationScreen(navController)
            }
        }

        // --- Authenticated Routes --- //

        composable(Screen.Home.route) {
            when {
                authState is AuthState.Authenticated ->
                    HomeScreen(navController, locationSearchViewModel, authViewModel)
                authState is AuthState.Unverified ->
                    AppScaffold(authViewModel = authViewModel, title = "SwayRider") { vm, _ ->
                        authState.profile?.let { profile ->
                            UnverifiedScreen(vm, profile)
                        }
                    }
                authState is AuthState.Unauthenticated ->
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
            }
        }

        composable(Screen.RoutePlanning.route) {
            if (authState is AuthState.Authenticated) {
                AppScaffold(
                    authViewModel = authViewModel,
                    title = "Route Planning"
                ) { _, _ ->
                    RoutePlanningScreen(navController)
                }
            } else if (authState is AuthState.Unauthenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.RoutePlanning.route) { inclusive = true }
                    }
                }
            }
        }
    }
}
