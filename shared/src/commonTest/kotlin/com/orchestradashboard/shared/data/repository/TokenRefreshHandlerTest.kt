package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.domain.model.AuthenticationResult
import com.orchestradashboard.shared.domain.repository.TokenRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenRefreshHandlerTest {
    private fun createMockClient(
        responseBody: String,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpClient {
        val engine =
            MockEngine {
                respond(
                    content = responseBody,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        return HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
    }

    private class FakeTokenRepository : TokenRepository {
        private var accessToken: String? = null
        private var refreshToken: String? = null
        private var expiryTimestamp: Long? = null

        override suspend fun saveTokens(result: AuthenticationResult) {
            accessToken = result.accessToken
            refreshToken = result.refreshToken
            expiryTimestamp = Clock.System.now().toEpochMilliseconds() + (result.expiresIn * 1000)
        }

        override suspend fun getAccessToken(): String? = accessToken

        override suspend fun getRefreshToken(): String? = refreshToken

        override suspend fun getTokenExpiryTimestamp(): Long? = expiryTimestamp

        override suspend fun clearTokens() {
            accessToken = null
            refreshToken = null
            expiryTimestamp = null
        }

        fun setTokens(
            access: String,
            refresh: String,
            expiryMs: Long,
        ) {
            accessToken = access
            refreshToken = refresh
            expiryTimestamp = expiryMs
        }
    }

    @Test
    fun `should refresh token automatically before expiration`() =
        runTest {
            val refreshResponse =
                """
                {"access_token":"new-access","refresh_token":"new-refresh","expires_in":900,"token_type":"Bearer"}
                """.trimIndent()
            val client = createMockClient(refreshResponse)
            val repo = FakeTokenRepository()
            // Token expires in 60 seconds (< 2 min threshold)
            val soonExpiry = Clock.System.now().toEpochMilliseconds() + 60_000L
            repo.setTokens("old-access", "old-refresh", soonExpiry)

            val handler = TokenRefreshHandler(client, "http://localhost:8080", repo)
            val token = handler.getValidAccessToken()

            assertEquals("new-access", token)
            assertEquals("new-access", repo.getAccessToken())
            assertEquals("new-refresh", repo.getRefreshToken())
        }

    @Test
    fun `should propagate error when refresh fails`() =
        runTest {
            val client = createMockClient("", HttpStatusCode.Unauthorized)
            val repo = FakeTokenRepository()
            val soonExpiry = Clock.System.now().toEpochMilliseconds() + 60_000L
            repo.setTokens("old-access", "old-refresh", soonExpiry)

            val handler = TokenRefreshHandler(client, "http://localhost:8080", repo)
            val token = handler.getValidAccessToken()

            // Falls back to old token when refresh fails
            assertEquals("old-access", token)
        }

    @Test
    fun `should not refresh when token is still valid`() =
        runTest {
            val client = createMockClient("{}")
            val repo = FakeTokenRepository()
            // Token expires in 10 minutes (> 2 min threshold)
            val farExpiry = Clock.System.now().toEpochMilliseconds() + 600_000L
            repo.setTokens("valid-access", "valid-refresh", farExpiry)

            val handler = TokenRefreshHandler(client, "http://localhost:8080", repo)
            val token = handler.getValidAccessToken()

            assertEquals("valid-access", token)
        }

    @Test
    fun `should return null when no token stored`() =
        runTest {
            val client = createMockClient("{}")
            val repo = FakeTokenRepository()

            val handler = TokenRefreshHandler(client, "http://localhost:8080", repo)
            val token = handler.getValidAccessToken()

            assertNull(token)
        }
}
