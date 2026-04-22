package com.hevanto_it.swayrider.core.network

import com.hevanto_it.swayrider.BuildConfig
import com.hevanto_it.swayrider.domain.auth.AuthService
import com.hevanto_it.swayrider.domain.auth.AuthStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Provides a configured [OkHttpClient] instance for network operations.
 *
 * This class is responsible for setting up the HTTP client with necessary interceptors and authenticators,
 * such as for handling authentication tokens and automatic token refreshing.
 *
 * @property authStorage The storage for retrieving the current authentication tokens.
 * @property authService A lazy-initialized [AuthService] used by the [TokenAuthenticator] to refresh tokens.
 */
class HttpClientProvider(
    authStorage: AuthStorage,
    authService: Lazy<AuthService>
) {
    private val authenticator = TokenAuthenticator(authStorage, authService)
    private val authInterceptor = AuthInterceptor(authStorage)

    /**
     * The configured [OkHttpClient] instance.
     * It includes an authenticator for automatic token refreshing and an interceptor to add auth tokens to headers.
     * Timeouts are configured based on the values in the `BuildConfig`.
     */
    val client: OkHttpClient = OkHttpClient.Builder()
        .authenticator(authenticator)
        .addInterceptor(authInterceptor)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .connectTimeout(BuildConfig.HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(BuildConfig.HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(BuildConfig.HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()
}