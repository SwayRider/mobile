package com.hevanto_it.swayrider.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hevanto_it.swayrider.domain.user.UserProfile
import com.hevanto_it.swayrider.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * A screen shown to users who have registered but not yet verified their email address.
 *
 * This screen provides the user with information about their unverified status and offers actions
 * to resolve it. It periodically checks the verification status in the background, allowing for
 * an automatic transition once the user is verified.
 *
 * @param authViewModel The [AuthViewModel] used to handle actions like resending the verification email,
 *                      checking verification status, and logging out.
 * @param profile The [UserProfile] of the currently logged-in user.
 */
@Composable
fun UnverifiedScreen(
    authViewModel: AuthViewModel,
    profile: UserProfile
) {
    // A side-effect that runs as long as the composable is on screen.
    // It periodically polls the backend to see if the user has been verified.
    LaunchedEffect(Unit) {
        while (isActive) {
            authViewModel.checkVerificationStatus()
            delay(3000L) // Poll every 3 seconds.
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Account Not Verified",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "The account for ${profile.email} has not been verified yet. " +
                "Please check your email for a verification link.",
        )

        Spacer(Modifier.height(24.dp))

        // Button to trigger the resending of the verification email.
        Button(
            onClick = { authViewModel.verifyEmail() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Resend Verification Email")
        }

        Spacer(Modifier.height(12.dp))

        // Button to allow the user to log out and switch accounts.
        OutlinedButton(
            onClick = { authViewModel.logout() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}