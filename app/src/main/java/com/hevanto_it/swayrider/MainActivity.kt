package com.hevanto_it.swayrider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.hevanto_it.swayrider.ui.theme.SwayRiderTheme

/**
 * The main and single activity of the SwayRider application.
 *
 * This activity serves as the entry point and the host for the entire Jetpack Compose UI.
 */
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is first created.
     *
     * This method sets up the UI by:
     * 1. Enabling edge-to-edge display for an immersive user experience.
     * 2. Setting the content to the [SwayRiderApp] composable, wrapped in the [SwayRiderTheme].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwayRiderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SwayRiderApp(this)
                }
            }
        }
    }
}
