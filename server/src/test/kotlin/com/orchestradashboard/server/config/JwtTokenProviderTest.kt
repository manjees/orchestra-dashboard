package com.orchestradashboard.server.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JwtTokenProviderTest {
    private val testSecret = "test-secret-key-that-is-at-least-32-bytes-long!!"
    private val accessExpirationMs = 900_000L
    private val refreshExpirationMs = 604_800_000L

    private val provider =
        JwtTokenProvider(
            secret = testSecret,
            accessExpirationMs = accessExpirationMs,
            refreshExpirationMs = refreshExpirationMs,
        )

    @Test
    fun `should generate non-blank access token for valid subject`() {
        val token = provider.generateAccessToken("dashboard-client")
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `should generate valid JWT when credentials match`() {
        val token = provider.generateAccessToken("dashboard-client")
        assertTrue(provider.validateToken(token))
        assertEquals("dashboard-client", provider.getSubject(token))
    }

    @Test
    fun `should validate a freshly generated token as valid`() {
        val token = provider.generateAccessToken("test-subject")
        assertTrue(provider.validateToken(token))
    }

    @Test
    fun `should reject token with tampered signature`() {
        val token = provider.generateAccessToken("dashboard-client")
        val tampered = token.dropLast(5) + "XXXXX"
        assertFalse(provider.validateToken(tampered))
    }

    @Test
    fun `should reject expired token`() {
        val shortLivedProvider =
            JwtTokenProvider(
                secret = testSecret,
                accessExpirationMs = 1L,
                refreshExpirationMs = 1L,
            )
        val token = shortLivedProvider.generateAccessToken("dashboard-client")
        Thread.sleep(50)
        assertFalse(shortLivedProvider.validateToken(token))
    }

    @Test
    fun `should extract correct subject from token`() {
        val token = provider.generateAccessToken("my-service")
        assertEquals("my-service", provider.getSubject(token))
    }

    @Test
    fun `should generate refresh token with longer expiry`() {
        val refreshToken = provider.generateRefreshToken("dashboard-client")
        assertNotNull(refreshToken)
        assertTrue(refreshToken.isNotBlank())
        assertTrue(provider.validateToken(refreshToken))
        assertEquals("dashboard-client", provider.getSubject(refreshToken))
    }
}
