package com.orchestradashboard.shared.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.orchestradashboard.shared.domain.model.AuthenticationResult
import com.orchestradashboard.shared.domain.repository.TokenRepository
import kotlinx.datetime.Clock

class AndroidTokenRepository(context: Context) : TokenRepository {
    private val prefs: SharedPreferences by lazy {
        val masterKey =
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    override suspend fun saveTokens(result: AuthenticationResult) {
        val expiryTimestamp = Clock.System.now().toEpochMilliseconds() + (result.expiresIn * 1000)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, result.accessToken)
            .putString(KEY_REFRESH_TOKEN, result.refreshToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryTimestamp)
            .apply()
    }

    override suspend fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    override suspend fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    override suspend fun getTokenExpiryTimestamp(): Long? {
        val v = prefs.getLong(KEY_TOKEN_EXPIRY, -1L)
        return if (v == -1L) null else v
    }

    override suspend fun clearTokens() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_FILE_NAME = "orchestra_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }
}
