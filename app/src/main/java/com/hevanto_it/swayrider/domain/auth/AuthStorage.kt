package com.hevanto_it.swayrider.domain.auth

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * A secure storage class for managing authentication-related data.
 *
 * This class uses [EncryptedSharedPreferences] to securely persist JWTs, refresh tokens,
 * and user credentials on the device.
 *
 * @property context The application context, used to initialize the master key and shared preferences.
 */
class AuthStorage(context: Context) {

    /** The master key used for encrypting the shared preferences. */
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    /** The instance of [EncryptedSharedPreferences] used for storing auth data. */
    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Saves the JWT and refresh token to secure storage.
     *
     * @param jwt The JSON Web Token.
     * @param refreshToken The refresh token.
     */
    fun saveTokens(jwt: String, refreshToken: String) {
        sharedPrefs.edit {
            putString("jwt", jwt)
            putString("refreshToken", refreshToken)
        }
    }

    /**
     * Retrieves the stored JWT.
     * @return The JWT as a [String], or `null` if not found.
     */
    fun getJwt(): String? = sharedPrefs.getString("jwt", null)

    /**
     * Retrieves the stored refresh token.
     * @return The refresh token as a [String], or `null` if not found.
     */
    fun getRefreshToken(): String? = sharedPrefs.getString("refreshToken", null)

    /**
     * Saves the user's credentials (email and password) to secure storage.
     * This can be used for features like automatic login.
     *
     * @param email The user's email.
     * @param password The user's password.
     */
    fun saveCredentials(email: String, password: String) {
        sharedPrefs.edit {
            putString("email", email)
            putString("password", password)
        }
    }

    /**
     * Retrieves the stored user credentials.
     * @return A [Pair] containing the email and password, or `null` if not found.
     */
    fun getCredentials(): Pair<String, String>? {
        val email = sharedPrefs.getString("email", null)
        val password = sharedPrefs.getString("password", null)
        return if (email != null && password != null) {
            Pair(email, password)
        } else {
            null
        }
    }

    /**
     * Clears all authentication-related data from storage.
     * This should be called on logout.
     */
    fun clearAll() {
        sharedPrefs.edit { clear() }
    }
}