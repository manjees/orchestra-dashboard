package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.domain.model.AuthenticationResult
import com.orchestradashboard.shared.domain.repository.TokenRepository
import java.util.prefs.Preferences

class DesktopTokenRepository : TokenRepository {
    private val prefs: Preferences = Preferences.userNodeForPackage(DesktopTokenRepository::class.java)

    override suspend fun saveTokens(result: AuthenticationResult) {
        val expiryTimestamp = System.currentTimeMillis() + (result.expiresIn * 1000)
        prefs.put(KEY_ACCESS_TOKEN, result.accessToken)
        prefs.put(KEY_REFRESH_TOKEN, result.refreshToken)
        prefs.putLong(KEY_TOKEN_EXPIRY, expiryTimestamp)
        prefs.flush()
    }

    override suspend fun getAccessToken(): String? = prefs.get(KEY_ACCESS_TOKEN, null)

    override suspend fun getRefreshToken(): String? = prefs.get(KEY_REFRESH_TOKEN, null)

    override suspend fun getTokenExpiryTimestamp(): Long? {
        val v = prefs.getLong(KEY_TOKEN_EXPIRY, -1L)
        return if (v == -1L) null else v
    }

    override suspend fun clearTokens() {
        prefs.remove(KEY_ACCESS_TOKEN)
        prefs.remove(KEY_REFRESH_TOKEN)
        prefs.remove(KEY_TOKEN_EXPIRY)
        prefs.flush()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }
}
