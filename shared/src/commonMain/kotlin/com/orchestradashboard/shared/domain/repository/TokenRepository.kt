package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.AuthenticationResult

interface TokenRepository {
    suspend fun saveTokens(result: AuthenticationResult)

    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun getTokenExpiryTimestamp(): Long?

    suspend fun clearTokens()
}
