package com.hevanto_it.swayrider.data.auth.remote

import com.hevanto_it.swayrider.core.network.AuthRequired
import com.hevanto_it.swayrider.core.network.NetworkResult
import com.hevanto_it.swayrider.core.network.safeApiCall
import com.hevanto_it.swayrider.data.auth.dto.*
import com.hevanto_it.swayrider.domain.auth.*
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineDispatcher
import retrofit2.http.*

/**
 * Retrofit API interface for authentication services.
 */
interface AuthServiceApi {
    @POST("check-password-strength")
    suspend fun checkPasswordStrength(@Body body: PasswordStrengthRequest) : PasswordStrengthResponse

    @POST("register")
    suspend fun register(@Body body: RegisterRequest) : RegisterResponse

    @POST("login")
    suspend fun login(@Body body: LoginRequest) : TokenResponse

    @POST("refresh")
    suspend fun refresh(@Body body: RefreshRequest) : TokenResponse

    @AuthRequired
    @GET("whoami")
    suspend fun whoAmI() : WhoAmIResponse

    @POST("verify-email")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest)

    @POST("request-password-reset")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest)
}

/**
 * Implementation of the [AuthService] interface.
 * This class uses Retrofit for network calls and wraps them with the [safeApiCall] helper
 * to provide structured success/error responses.
 *
 * @property authApi The Retrofit API service.
 * @property dispatcher The coroutine dispatcher for background tasks.
 * @property moshi The Moshi instance for JSON parsing, especially for error bodies.
 */
class AuthServiceImpl(
    private val authApi: AuthServiceApi,
    private val dispatcher: CoroutineDispatcher,
    private val moshi: Moshi
) : AuthService {
    override suspend fun checkPasswordStrength(password: String): NetworkResult<PasswordStrength> {
        return when (val result = safeApiCall(dispatcher, moshi) { authApi.checkPasswordStrength(PasswordStrengthRequest(password)) }) {
            is NetworkResult.Success -> NetworkResult.Success(PasswordStrength(result.data.isStrong, result.data.message))
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }

    override suspend fun register(email: String, password: String, verificationUrl: String): NetworkResult<RegistrationResult> {
        return when (val result = safeApiCall(dispatcher, moshi) { authApi.register(RegisterRequest(email, password, verificationUrl)) }) {
            is NetworkResult.Success -> NetworkResult.Success(RegistrationResult(result.data.userId, result.data.message))
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }

    override suspend fun login(email: String, password: String): NetworkResult<TokenPair> {
        return when (val result = safeApiCall(dispatcher, moshi) { authApi.login(LoginRequest(email, password)) }) {
            is NetworkResult.Success -> NetworkResult.Success(TokenPair(result.data.accessToken, result.data.refreshToken))
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }

    override suspend fun refresh(refreshToken: String): NetworkResult<TokenPair> {
        return when (val result = safeApiCall(dispatcher, moshi) { authApi.refresh(RefreshRequest(refreshToken)) }) {
            is NetworkResult.Success -> NetworkResult.Success(TokenPair(result.data.accessToken, result.data.refreshToken))
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }


    override suspend fun whoAmI(): NetworkResult<WhoAmI> {
        println("DEBUG: Entering safeApiCall for whoAmI")
        val result = safeApiCall(dispatcher, moshi) {
            println("DEBUG: Executing Retrofit call...")
            authApi.whoAmI()
        }
        println("DEBUG: Exited safeApiCall with result: $result")

        return when (result) {
            is NetworkResult.Success -> {
                val response = result.data
                NetworkResult.Success(WhoAmI(response.userId, response.email, response.isVerified, response.isAdmin, response.accountType))
            }
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }

    /*override suspend fun whoAmI(): NetworkResult<WhoAmI> {
        return when (val result = safeApiCall(dispatcher, moshi) { authApi.whoAmI() }) {
            is NetworkResult.Success -> {
                val response = result.data
                NetworkResult.Success(WhoAmI(response.userId, response.email, response.isVerified, response.isAdmin, response.accountType))
            }
            is NetworkResult.Error -> result
            is NetworkResult.Exception -> result
        }
    }*/

    override suspend fun verifyEmail(email: String, verificationUrl: String): NetworkResult<Unit> {
        return safeApiCall(dispatcher, moshi) {
            authApi.verifyEmail(VerifyEmailRequest(email, verificationUrl))
        }
    }

    override suspend fun forgotPassword(email: String, resetUrl: String): NetworkResult<Unit> {
        return safeApiCall(dispatcher, moshi) {
            authApi.forgotPassword(ForgotPasswordRequest(email, resetUrl))
        }
    }
}