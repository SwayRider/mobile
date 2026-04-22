package com.hevanto_it.swayrider.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hevanto_it.swayrider.domain.auth.PasswordStrengthState
import com.hevanto_it.swayrider.ui.navigation.Screen
import com.hevanto_it.swayrider.viewmodel.AuthViewModel

/**
 * A composable screen for new user registration.
 *
 * This screen provides a form with fields for email, password, and password confirmation.
 * It features real-time validation for:
 * - Email format.
 * - Password strength (by observing [AuthViewModel.passwordStrengthState]).
 * - Password matching.
 * The registration button is only enabled when all validation criteria are met.
 *
 * @param navController The [NavController] for navigating to other screens.
 * @param authViewModel The [AuthViewModel] to handle registration logic and password strength checks.
 */
@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordCheck by remember { mutableStateOf("") }

    val passwordStrength by authViewModel.passwordStrengthState.collectAsState()

    // Reset the password strength state when the user leaves the screen.
    DisposableEffect(Unit) {
        onDispose {
            authViewModel.resetPasswordStrengthState()
        }
    }

    val passwordFocusRequester = remember { FocusRequester() }
    val passwordCheckFocusRequester = remember { FocusRequester() }

    // Determines if the registration button should be enabled.
    val isButtonEnabled = remember(email, password, passwordCheck, passwordStrength) {
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val arePasswordsMatching = password.isNotEmpty() && password == passwordCheck
        isEmailValid && arePasswordsMatching && passwordStrength is PasswordStrengthState.Strong
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches(),
            supportingText = {
                if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Text("Invalid email address")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() }
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                authViewModel.onPasswordChanged(it) // Check strength on change
            },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester),
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordStrength is PasswordStrengthState.Weak,
            supportingText = {
                if (passwordStrength is PasswordStrengthState.Weak) {
                    Text("Password is too weak")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordCheckFocusRequester.requestFocus() }
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password Confirmation Field
        OutlinedTextField(
            value = passwordCheck,
            onValueChange = { passwordCheck = it },
            label = { Text("Confirm Password") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordCheckFocusRequester),
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordCheck.isNotEmpty() && password != passwordCheck,
            supportingText = {
                if (passwordCheck.isNotEmpty() && password != passwordCheck) {
                    Text("Passwords do not match")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isButtonEnabled) {
                        authViewModel.register(email, password)
                    }
                }
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Registration Button
        Button(
            onClick = { authViewModel.register(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isButtonEnabled
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(64.dp))

        // Link to Login Screen
        OutlinedButton(
            onClick = {
                navController.navigate(Screen.Login.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login with existing account")
        }
    }
}