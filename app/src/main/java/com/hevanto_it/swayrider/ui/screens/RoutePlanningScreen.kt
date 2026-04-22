package com.hevanto_it.swayrider.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * A placeholder screen for the route planning feature.
 *
 * This composable is intended to house the UI for planning new routes. It is accessible to
 * authenticated users and currently displays a placeholder text.
 *
 * @param navController The [NavController] for handling navigation events (currently unused).
 */
@Composable
fun RoutePlanningScreen(
    navController: NavController,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Route planning will be implemented here.")
    }
}