package com.hevanto_it.swayrider.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hevanto_it.swayrider.BuildConfig
import com.hevanto_it.swayrider.core.network.NetworkResult
import com.hevanto_it.swayrider.domain.auth.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing the authentication state and user authentication flows.
 *
 * This ViewModel handles login, registration, logout, and token management. It communicates with
 * [AuthService] for network operations and [AuthStorage] for persisting authentication data.
 *
 * @property authStorage The repository for storing and retrieving authentication tokens and credentials.
 * @property authService The service for performing authentication-related network requests.
 */
class AuthViewModel(
    private val authStorage: AuthStorage,
    private val authService: AuthService
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    /** Exposes the current authentication state of the user. */
    val authState: StateFlow<AuthState> = _authState

    private val _passwordStrengthState = MutableStateFlow<PasswordStrengthState>(PasswordStrengthState.Idle)
    /** Exposes the current state of the password strength check. */
    val passwordStrengthState: StateFlow<PasswordStrengthState> = _passwordStrengthState

    private var passwordCheckJob: Job? = null
    private val debouncePeriodMs = 500L

    private val _events = MutableSharedFlow<AuthEvent>()
    /** A flow of one-time events for the UI, such as showing error messages. */
    val events: SharedFlow<AuthEvent> = _events

    /** The URL used for the email verification link. */
    val verifyUrl = "${BuildConfig.AUTH_SERVICE_WEB_HOST}:${BuildConfig.AUTH_SERVICE_WEB_PORT}${BuildConfig.AUTH_SERVICE_WEB_PREFIX}verify-user"

    /** The URL embedded in the password reset email link. */
    val forgotPasswordResetUrl = "${BuildConfig.AUTH_SERVICE_WEB_HOST}:${BuildConfig.AUTH_SERVICE_WEB_PORT}${BuildConfig.AUTH_SERVICE_WEB_PREFIX}reset-password"

    init {
        println("DEBUG: AuthViewModel init started")
        authenticate()
    }

    /**
     * Attempts to authenticate the user by checking for existing tokens or credentials.
     */
    private fun authenticate() {
        viewModelScope.launch {
            println("DEBUG: authenticate() started, setting state to Loading")
            _authState.value = AuthState.Loading

            // 1. JWT check
            val jwt = authStorage.getJwt()
            println("DEBUG: JWT found: ${jwt != null}")
            if (jwt != null) {
                println("DEBUG: Calling whoAmI with JWT")
                callWhoAmI()
                println("DEBUG: After whoAmI, state is: ${authState.value}")
                if (authState.value !is AuthState.Unauthenticated && authState.value !is AuthState.Loading) {
                    println("DEBUG: Authenticated via JWT")
                    return@launch
                }
            }

            // 2. RefreshToken check
            val refreshToken = authStorage.getRefreshToken()
            println("DEBUG: RefreshToken found: ${refreshToken != null}")
            if (refreshToken != null) {
                println("DEBUG: Calling refresh with token")
                callRefresh(refreshToken)
                println("DEBUG: After refresh, state is: ${authState.value}")
                if (authState.value !is AuthState.Unauthenticated && authState.value !is AuthState.Loading) {
                    println("DEBUG: Authenticated via RefreshToken")
                    return@launch
                }
            }

            // 3. Username/Password
            val credentials = authStorage.getCredentials()
            println("DEBUG: Credentials found: ${credentials != null}")
            if (credentials != null) {
                val (email, password) = credentials
                println("DEBUG: Calling login with stored credentials for $email")
                callLogin(email, password)
                println("DEBUG: After login, state is: ${authState.value}")
                if (authState.value !is AuthState.Unauthenticated && authState.value !is AuthState.Loading) {
                    println("DEBUG: Authenticated via credentials")
                    return@launch
                }
            }

            // 4. Auth failure --> Go to login screen
            println("DEBUG: All auth methods failed. Clearing storage and setting Unauthenticated.")
            authStorage.clearAll()
            _authState.value = AuthState.Unauthenticated
            println("DEBUG: authenticate() finished - state: ${_authState.value}")
        }
    }

    /**
     * Registers a new user with the given email and password.
     */
    fun register(email: String, password: String) {
        viewModelScope.launch {
            println("DEBUG: register() for $email")
            callRegister(email, password)
            if (authState.value is AuthState.Unauthenticated) {
                println("DEBUG: Registration failed or returned Unauthenticated")
                authStorage.clearAll()
                _events.emit(AuthEvent.ShowError("register failed"))
            }
        }
    }

    /**
     * Logs the user in with the given email and password.
     */
    fun login(email: String, password: String, rememberMe: Boolean = false) {
        viewModelScope.launch {
            println("DEBUG: login() for $email")
            callLogin(email, password, rememberMe)
            if (authState.value is AuthState.Unauthenticated) {
                println("DEBUG: Login failed or returned Unauthenticated")
                authStorage.clearAll()
                _events.emit(AuthEvent.ShowError("login failed"))
            }
        }
    }

    /**
     * Logs the user out by clearing all stored authentication data and resetting the state.
     */
    fun logout() {
        viewModelScope.launch {
            println("DEBUG: logout() called")
            authStorage.clearAll()
            _authState.value = AuthState.Unauthenticated
            _events.emit(AuthEvent.LoggedOut)
        }
    }

    /**
     * Requests a new verification email to be sent to the user's email address.
     */
    fun verifyEmail() {
        if (authState.value !is AuthState.Unverified) {
            println("DEBUG: verifyEmail() called but state is not Unverified (is ${authState.value})")
            return
        }
        viewModelScope.launch {
            val profile = authState.value.profile
            if (profile == null) {
                println("DEBUG: verifyEmail() - No profile found in state")
                logout()
                _events.emit(AuthEvent.ShowError("No profile found"))
                return@launch
            }

            println("DEBUG: verifyEmail() requesting for ${profile.email}")
            authService.verifyEmail(profile.email, verifyUrl)
        }
    }

    /**
     * Checks the user's verification status by calling the `whoAmI` endpoint.
     */
    fun checkVerificationStatus() {
        if (authState.value is AuthState.Loading) return
        viewModelScope.launch {
            println("DEBUG: checkVerificationStatus() starting")
            if (authStorage.getJwt() == null) {
                println("DEBUG: checkVerificationStatus() - No JWT found")
                return@launch
            }
            callWhoAmI()
            val state = authState.value
            println("DEBUG: checkVerificationStatus() after whoAmI, state: $state")
            if (state is AuthState.Unauthenticated) {
                logout()
                return@launch
            }
            val profile = state.profile
            if (profile == null) {
                logout()
                return@launch
            }
            if (profile.isVerified) {
                println("DEBUG: User is now verified!")
                _authState.value = AuthState.Authenticated(profile)
            }
        }
    }

    /**
     * Fetches the current user's profile information from the backend.
     */
    private suspend fun callWhoAmI() {
        println("DEBUG: callWhoAmI() executing network request")
        val whoAmIResult = authService.whoAmI()
        println("DEBUG: whoAmIResult: $whoAmIResult")
        when(whoAmIResult) {
            is NetworkResult.Success -> {
                val whoAmI = whoAmIResult.data
                println("DEBUG: whoAmI Success - verified: ${whoAmI.isVerified}")
                if (whoAmI.isVerified) {
                    _authState.value = AuthState.Authenticated(whoAmI.toDomain())
                } else {
                    _authState.value = AuthState.Unverified(whoAmI.toDomain())
                }
            }
            is NetworkResult.Error -> {
                val error = whoAmIResult.error
                println("DEBUG: whoAmI Error: ${error.message}")
                _events.emit(AuthEvent.ShowError(error.message))
                _authState.value = AuthState.Unauthenticated
            }
            is NetworkResult.Exception -> {
                val exception = whoAmIResult.throwable
                println("DEBUG: whoAmI Exception: ${exception.message}")
                _events.emit(AuthEvent.ShowError(exception.message ?: "Unknown error"))
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Refreshes the authentication tokens using the provided refresh token.
     */
    private suspend fun callRefresh(refreshToken: String) {
        println("DEBUG: callRefresh() executing network request")
        val refreshResult = authService.refresh(refreshToken)
        println("DEBUG: refreshResult: $refreshResult")
        when (refreshResult) {
            is NetworkResult.Success -> {
                val refresh = refreshResult.data
                authStorage.saveTokens(refresh.jwt, refresh.refresh)
                callWhoAmI()
            }
            else -> {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Performs a login request.
     */
    private suspend fun callLogin(email: String, password: String, rememberMe: Boolean = false) {
        println("DEBUG: callLogin() executing network request for $email")
        val loginResult = authService.login(email, password)
        println("DEBUG: loginResult: $loginResult")
        when (loginResult) {
            is NetworkResult.Success -> {
                val login = loginResult.data
                authStorage.saveTokens(login.jwt, login.refresh)
                if (rememberMe) authStorage.saveCredentials(email, password)
                callWhoAmI()
            }
            else -> {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    /**
     * Performs a registration request.
     */
    private suspend fun callRegister(email: String, password: String) {
        println("DEBUG: callRegister() executing network request for $email")
        val registerResult = authService.register(email, password, verifyUrl)
        println("DEBUG: registerResult: $registerResult")
        when (registerResult) {
            is NetworkResult.Success -> {
                callLogin(email, password)
            }
            is NetworkResult.Error -> {
                val error = registerResult.error
                _authState.value = AuthState.Unauthenticated
                _events.emit(AuthEvent.ShowError(error.message))
            }
            else -> {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    // ... other methods omitted for brevity as requested or kept same
    fun onPasswordChanged(password: String) {
        passwordCheckJob?.cancel()
        if (password.isBlank()) {
            _passwordStrengthState.value = PasswordStrengthState.Idle
            return
        }

        passwordCheckJob = viewModelScope.launch{
            _passwordStrengthState.value = PasswordStrengthState.Checking
            delay(debouncePeriodMs)
            val passwordCheckResult = authService.checkPasswordStrength(password)
            when(passwordCheckResult) {
                is NetworkResult.Success -> {
                    val passwordCheck = passwordCheckResult.data
                    _passwordStrengthState.value = if (passwordCheck.isStrong) {
                        PasswordStrengthState.Strong
                    } else {
                        PasswordStrengthState.Weak
                    }
                }
                else -> {
                    _passwordStrengthState.value = PasswordStrengthState.Idle
                }
            }
        }
    }

    fun resetPasswordStrengthState() {
        _passwordStrengthState.value = PasswordStrengthState.Idle
    }

    /**
     * Sends a password reset email to the provided address.
     * Always emits [AuthEvent.ForgotPasswordSent] on success (even if the email is unknown,
     * to prevent enumeration). Emits [AuthEvent.ShowError] on network failure.
     */
    fun forgotPassword(email: String) {
        viewModelScope.launch {
            when (val result = authService.forgotPassword(email, forgotPasswordResetUrl)) {
                is NetworkResult.Success -> _events.emit(AuthEvent.ForgotPasswordSent)
                is NetworkResult.Error -> _events.emit(AuthEvent.ShowError(result.error.message))
                is NetworkResult.Exception -> _events.emit(AuthEvent.ShowError(result.throwable.message ?: "Unknown error"))
            }
        }
    }
}

class AuthViewModelFactory(
    private val authStorage: AuthStorage,
    private val authService: AuthService
) : ViewModelProvider.Factory {
    override fun<T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authStorage, authService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
