package com.hevanto_it.swayrider.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.hevanto_it.swayrider.domain.auth.AuthState
import com.hevanto_it.swayrider.viewmodel.AuthViewModel

/**
 * A general-purpose scaffold for screens that require authentication.
 *
 * This composable provides a consistent layout with a [TopAppBar] that includes a title and
 * a [ProfileMenu] (if the user is fully authenticated). The main screen content is placed
 * within the body of the scaffold, with appropriate padding applied.
 *
 * @param authViewModel The [AuthViewModel] instance, used to observe the auth state and pass to content.
 * @param title The title to be displayed in the [TopAppBar].
 * @param content The main content of the screen. This is a lambda that receives the [AuthViewModel]
 *                and the current [AuthState], allowing the content to be reactive.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    authViewModel: AuthViewModel,
    title: String,
    content: @Composable (AuthViewModel, AuthState) -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    // Only show the profile menu if the user is fully authenticated.
                    if (authState is AuthState.Authenticated) {
                        ProfileMenu(authViewModel)
                    }
                }
            )
        },
    ) { padding ->
        // The main content area, with padding applied to avoid overlapping with the TopAppBar.
        Box(modifier = Modifier.padding(padding)) {
            content(authViewModel, authState)
        }
    }
}