package com.hevanto_it.swayrider

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.hevanto_it.swayrider.core.network.HttpClientProvider
import com.hevanto_it.swayrider.data.auth.remote.AuthServiceApi
import com.hevanto_it.swayrider.data.auth.remote.AuthServiceImpl
import com.hevanto_it.swayrider.data.search.LocationSearchRepositoryImpl
import com.hevanto_it.swayrider.data.search.remote.SearchServiceApi
import com.hevanto_it.swayrider.domain.auth.AuthService
import com.hevanto_it.swayrider.domain.auth.AuthState
import com.hevanto_it.swayrider.domain.auth.AuthStorage
import com.hevanto_it.swayrider.domain.search.LocationSearchRepository
import com.hevanto_it.swayrider.ui.navigation.Screen
import com.hevanto_it.swayrider.viewmodel.AuthEvent
import com.hevanto_it.swayrider.viewmodel.AuthViewModel
import com.hevanto_it.swayrider.viewmodel.AuthViewModelFactory
import com.hevanto_it.swayrider.viewmodel.LocationSearchViewModel
import com.hevanto_it.swayrider.viewmodel.LocationSearchViewModelFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * The root composable of the SwayRider application.
 *
 * This function is responsible for:
 * - Setting up a simple manual dependency injection (DI) container.
 * - Initializing the [AuthViewModel].
 * - Observing the authentication state and handling UI events.
 * - Displaying the main navigation structure ([SwayRiderNavHost]) and a global loading indicator.
 *
 * @param mainActivity The instance of the [MainActivity], used to access the application context and show Toasts.
 */
@Composable
fun SwayRiderApp(
    mainActivity: MainActivity
) {
    val authStorage = remember { AuthStorage(mainActivity.applicationContext) }

    // A simple, remembered object to act as a manual Dependency Injection container.
    val diContainer = remember {
        object {
            val moshi: Moshi by lazy {
                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            }

            val ioDispatcher: CoroutineDispatcher by lazy {
                Dispatchers.IO
            }

            val authApi: AuthServiceApi by lazy {
                authRetrofit.create(AuthServiceApi::class.java)
            }

            val authService: AuthService by lazy {
                AuthServiceImpl(authApi, ioDispatcher, moshi)
            }

            val authHttpClientProvider: HttpClientProvider by lazy {
                HttpClientProvider(
                    authStorage = authStorage,
                    // The 'authService' is only invoked when the TokenAuthenticator really needs it,
                    // avoiding a circular dependency issue during initialization.
                    authService = lazy { authService }
                )
            }
            val authRetrofit: Retrofit by lazy {
                Retrofit.Builder()
                    .baseUrl("${BuildConfig.AUTH_SERVICE_HOST}:${BuildConfig.AUTH_SERVICE_PORT}${BuildConfig.AUTH_SERVICE_PREFIX}")
                    .callFactory { request -> authHttpClientProvider.client.newCall(request) }
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
            }

            val searchServiceApi: SearchServiceApi by lazy {
                Retrofit.Builder()
                    .baseUrl("${BuildConfig.SEARCH_SERVICE_HOST}:${BuildConfig.SEARCH_SERVICE_PORT}${BuildConfig.SEARCH_SERVICE_PREFIX}/")
                    .callFactory { request -> authHttpClientProvider.client.newCall(request) }
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                    .create(SearchServiceApi::class.java)
            }

            val locationSearchRepository: LocationSearchRepository by lazy {
                LocationSearchRepositoryImpl(searchServiceApi, ioDispatcher, moshi)
            }

            val locationSearchViewModelFactory: LocationSearchViewModelFactory by lazy {
                LocationSearchViewModelFactory(locationSearchRepository)
            }
        }
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authStorage, diContainer.authService)
    )

    val locationSearchViewModel: LocationSearchViewModel = viewModel(
        factory = diContainer.locationSearchViewModelFactory
    )

    val authState by authViewModel.authState.collectAsState()
    val navController = rememberNavController()

    // Handles one-time events from the ViewModel, like showing toasts or navigating on logout.
    LaunchedEffect(Unit) {
        authViewModel.events.collect { event ->
            when (event) {
                is AuthEvent.ShowError -> {
                    Toast.makeText(mainActivity, event.message, Toast.LENGTH_SHORT).show()
                }
                is AuthEvent.LoggedOut -> {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                is AuthEvent.ForgotPasswordSent -> Unit // handled in ForgotPasswordScreen
            }
        }
    }

    // The main layout, containing the navigation host and a global loading indicator.
    Box(Modifier.fillMaxSize()) {
        SwayRiderNavHost(
            navController = navController,
            authViewModel = authViewModel,
            authState = authState,
            locationSearchViewModel = locationSearchViewModel
        )

        // Show a loading indicator overlay when the authentication state is loading.
        if (authState is AuthState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
