package com.hevanto_it.swayrider.core.network

import com.hevanto_it.swayrider.domain.auth.AuthStorage
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation

/**
 * An [Interceptor] that adds an `Authorization` header with a bearer token to outgoing requests.
 *
 * This interceptor checks if a Retrofit method is annotated with [@AuthRequired]. If it is, and a JWT is available
 * in [AuthStorage], it attaches the token to the request.
 *
 * @property authStorage The storage for retrieving the current JWT.
 */
class AuthInterceptor(
    private val authStorage: AuthStorage
) : Interceptor {
    /**
     * Intercepts the request and adds the Authorization header if required.
     */
    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val originalRequest = chain.request()
        val invocation = originalRequest.tag(Invocation::class.java)

        // Check if the endpoint is annotated with @AuthRequired
        val authRequired = invocation?.method()?.isAnnotationPresent(AuthRequired::class.java) == true

        if (authRequired) {
            val jwt = authStorage.getJwt()
            if (jwt != null) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $jwt")
                    .build()
                return chain.proceed(newRequest)
            }
            // Note: If auth is required but no JWT is present, the request proceeds without it.
            // The server is expected to return a 401 Unauthorized, which will be handled by the TokenAuthenticator.
        }

        return chain.proceed(originalRequest)
    }
}
