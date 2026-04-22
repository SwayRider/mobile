package com.hevanto_it.swayrider.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.hevanto_it.swayrider.viewmodel.AuthViewModel

/**
 * A composable that displays a profile icon which, upon clicking, reveals a dropdown menu.
 *
 * This menu contains user-specific actions, such as logging out.
 *
 * @param authViewModel The [AuthViewModel] used to handle actions triggered from the menu, like logging out.
 */
@Composable
fun ProfileMenu(authViewModel: AuthViewModel) {
    // State to control whether the dropdown menu is expanded or collapsed.
    var expanded by remember { mutableStateOf(false) }

    // The icon button that the user clicks to open the menu.
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile"
        )
    }

    // The dropdown menu itself.
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false } // Closes the menu when the user clicks outside of it.
    ) {
        // The "Logout" menu item.
        DropdownMenuItem(
            text = { Text("Logout") },
            onClick = {
                expanded = false // Close the menu.
                authViewModel.logout() // Trigger the logout process.
            }
        )
    }
}