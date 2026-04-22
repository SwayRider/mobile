package com.hevanto_it.swayrider.core.network

import com.hevanto_it.swayrider.domain.auth.AuthService
import com.hevanto_it.swayrider.domain.auth.AuthStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * An OkHttp [Authenticator] that automatically refreshes expired authentication tokens.
 *
 * This class intercepts 401 Unauthorized responses. It then attempts to use a stored refresh token
 * to obtain a new pair of access and refresh tokens from the [AuthService].
 *
 * Note: This class uses [runBlocking] to execute the suspendable `refresh` function. This is necessary
 * because the OkHttp `Authenticator` interface is synchronous. This blocks the networking thread, but only
 * for the duration of the token refresh API call.
 *
 * @property authStorage The storage for retrieving the refresh token and saving new tokens.
 * @property authServiceProvider A lazy-initialized [AuthService] to avoid dependency cycles, used to perform the token refresh.
 */
class TokenAuthenticator(
    private val authStorage: AuthStorage,
    private val authServiceProvider: Lazy<AuthService>
) : Authenticator {

    /**
     * Called when an HTTP response indicates that authentication is required (401 Unauthorized).
     *
     * @return A new request with the new token, or null if the refresh attempt fails.
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops: if the refresh endpoint itself returns 401, give up immediately.
        // Without this check, a failed refresh triggers another refresh attempt, causing
        // infinite recursive runBlocking calls that permanently hang the OkHttp thread pool.
        if (response.request.url.encodedPath.endsWith("/refresh")) return null

        val authService = authServiceProvider.value
        // We need a refresh token to proceed.
        val refreshToken = authStorage.getRefreshToken() ?: return null

        // Perform the token refresh call synchronously.
        val newTokens = runBlocking {
            when (val refreshResult = authService.refresh(refreshToken)) {
                is NetworkResult.Success -> refreshResult.data
                else -> null // Refresh failed.
            }
        }

        return if (newTokens != null) {
            // If refresh was successful, save the new tokens.
            authStorage.saveTokens(newTokens.jwt, newTokens.refresh)
            // Retry the original request with the new access token.
            response.request.newBuilder()
                .header("Authorization", "Bearer ${newTokens.jwt}")
                .build()
        } else {
            // If refresh failed, clear all stored data and cancel authentication.
            authStorage.clearAll()
            null
        }
    }
}