package com.hevanto_it.swayrider.core.network

import com.hevanto_it.swayrider.data.auth.dto.ApiError
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * A wrapper function that executes a suspendable API call and encapsulates the result in a [NetworkResult] object.
 * This function centralizes error handling for network requests.
 *
 * It catches [IOException] for network-related issues and [HttpException] for non-2xx server responses.
 * For [HttpException], it attempts to parse the error body into an [ApiError] object.
 *
 * @param T The success type of the API call.
 * @param dispatcher The [CoroutineDispatcher] to execute the call on, typically `Dispatchers.IO`.
 * @param moshi The [Moshi] instance used to parse error bodies.
 * @param apiCall The suspendable lambda function representing the actual API call.
 * @return A [NetworkResult] which is either [NetworkResult.Success], [NetworkResult.Error], or [NetworkResult.Exception].
 */
suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, moshi: Moshi, apiCall: suspend () -> T): NetworkResult<T> {
    return withContext(dispatcher) {
        try {
            NetworkResult.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is IOException -> NetworkResult.Exception(throwable)
                is HttpException -> {
                    val apiError = convertErrorBody(throwable, moshi)
                    if (apiError != null) {
                        NetworkResult.Error(apiError)
                    } else {
                        NetworkResult.Exception(IOException("Unexpected error: ${throwable.message()}", throwable))
                    }
                }
                else -> NetworkResult.Exception(throwable)
            }
        }
    }
}

/**
 * Attempts to parse an [HttpException]'s error body into an [ApiError] object.
 *
 * @param throwable The [HttpException] to parse.
 * @param moshi The [Moshi] instance for JSON conversion.
 * @return An [ApiError] object if parsing is successful, otherwise `null`.
 */
private fun convertErrorBody(throwable: HttpException, moshi: Moshi): ApiError? {
    return try {
        throwable.response()?.errorBody()?.source()?.let {
            val adapter = moshi.adapter(ApiError::class.java)
            adapter.fromJson(it)
        }
    } catch (exception: Exception) {
        null
    }
}