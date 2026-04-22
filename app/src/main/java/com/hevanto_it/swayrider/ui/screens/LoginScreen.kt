package com.hevanto_it.swayrider.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.hevanto_it.swayrider.ui.navigation.Screen
import com.hevanto_it.swayrider.viewmodel.AuthViewModel

/**
 * A composable screen for user login.
 *
 * This screen provides input fields for email and password, a login button, and a button
 * to navigate to the registration screen. It features basic form validation and a user-friendly
 * keyboard flow.
 *
 * @param navController The [NavController] for navigating to other screens, like registration.
 * @param authViewModel The [AuthViewModel] to handle the login logic.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }

    // FocusRequester to programmatically move focus to the password field.
    val passwordFocusRequester = remember { FocusRequester() }

    // Enable the login button only when the email is valid and the password is of a minimum length.
    val isButtonEnabled = remember(email, password) {
        Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 8
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, // Use the appropriate keyboard for email input.
                imeAction = ImeAction.Next // Show a "Next" button on the keyboard.
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocusRequester.requestFocus() } // Move focus when the "Next" action is triggered.
            ),
            singleLine = true // Ensures the user cannot enter new lines.
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocusRequester), // Attach the FocusRequester.
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done // "Done" is appropriate for the final field.
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isButtonEnabled) {
                        // Trigger the login action when "Done" is pressed on the keyboard.
                        authViewModel.login(email, password, rememberMe)
                    }
                }
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
            Text("Remember me")
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(
            onClick = { navController.navigate(Screen.ForgotPassword.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forgot password?")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = { authViewModel.login(email, password, rememberMe) },
            modifier = Modifier.fillMaxWidth(),
            enabled = isButtonEnabled
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(64.dp))
        OutlinedButton(
            onClick = {
                navController.navigate(Screen.Register.route)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create new account")
        }
    }
}