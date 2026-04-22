package com.hevanto_it.swayrider.core.network

import com.hevanto_it.swayrider.data.auth.dto.ApiError

/**
 * A sealed class that encapsulates the result of a network request.
 * It can be in one of three states: Success, Error, or Exception.
 *
 * @param T The type of the successful data.
 */
sealed class NetworkResult<out T> {
    /**
     * Represents a successful network request.
     * @property data The data returned by the server.
     */
    data class Success<T>(val data: T) : NetworkResult<T>()

    /**
     * Represents a specific error response from the API.
     * This is used when the server returns a structured error message (e.g., a JSON error object).
     * @property error The [ApiError] object containing details about the error.
     */
    data class Error(val error: ApiError) : NetworkResult<Nothing>()

    /**
     * Represents a failure that occurred during the network request, such as a network connectivity issue
     * or an unexpected exception.
     * @property throwable The underlying [Throwable] that caused the failure.
     */
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()
}