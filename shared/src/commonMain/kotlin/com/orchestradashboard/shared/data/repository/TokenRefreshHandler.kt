package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AuthResponseDto
import com.orchestradashboard.shared.data.dto.RefreshRequestDto
import com.orchestradashboard.shared.domain.model.AuthenticationResult
import com.orchestradashboard.shared.domain.repository.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class TokenRefreshHandler(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val tokenRepository: TokenRepository,
) {
    private val refreshMutex = Mutex()

    suspend fun getValidAccessToken(): String? {
        val token = tokenRepository.getAccessToken() ?: return null
        val expiry = tokenRepository.getTokenExpiryTimestamp() ?: return token
        val now = Clock.System.now().toEpochMilliseconds()

        return if (expiry - now < REFRESH_THRESHOLD_MS) {
            refreshToken() ?: token
        } else {
            token
        }
    }

    private suspend fun refreshToken(): String? =
        refreshMutex.withLock {
            val expiry = tokenRepository.getTokenExpiryTimestamp() ?: return null
            val now = Clock.System.now().toEpochMilliseconds()
            if (expiry - now >= REFRESH_THRESHOLD_MS) {
                return tokenRepository.getAccessToken()
            }

            val refreshToken = tokenRepository.getRefreshToken() ?: return null
            @Suppress("TooGenericExceptionCaught")
            return try {
                val response: AuthResponseDto =
                    httpClient.post("$baseUrl/api/v1/auth/refresh") {
                        contentType(ContentType.Application.Json)
                        setBody(RefreshRequestDto(refreshToken))
                    }.body()
                val result =
                    AuthenticationResult(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        expiresIn = response.expiresIn,
                    )
                tokenRepository.saveTokens(result)
                response.accessToken
            } catch (e: Exception) {
                null
            }
        }

    companion object {
        // 2 minutes before expiry
        const val REFRESH_THRESHOLD_MS = 120_000L
    }
}
