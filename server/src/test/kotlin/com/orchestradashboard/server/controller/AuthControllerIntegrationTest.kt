package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.model.AuthRequest
import com.orchestradashboard.server.model.RefreshRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `POST auth-login with valid credentials returns tokens`() {
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthRequest(apiKey = "dev-api-key"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.access_token") { isNotEmpty() }
            jsonPath("$.refresh_token") { isNotEmpty() }
            jsonPath("$.expires_in") { isNumber() }
            jsonPath("$.token_type") { value("Bearer") }
        }
    }

    @Test
    fun `POST auth-login with invalid credentials returns 401`() {
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthRequest(apiKey = "wrong-key"))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST auth-refresh with valid refresh token returns new access token`() {
        val refreshToken = jwtTokenProvider.generateRefreshToken("dashboard-client")

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshRequest(refreshToken = refreshToken))
        }.andExpect {
            status { isOk() }
            jsonPath("$.access_token") { isNotEmpty() }
            jsonPath("$.refresh_token") { isNotEmpty() }
        }
    }

    @Test
    fun `POST auth-refresh with expired token returns 401`() {
        val expiredProvider =
            JwtTokenProvider(
                secret = "default-dev-secret-that-is-at-least-32-bytes-long!!",
                accessExpirationMs = 1L,
                refreshExpirationMs = 1L,
            )
        val expiredToken = expiredProvider.generateRefreshToken("dashboard-client")
        Thread.sleep(50)

        mockMvc.post("/api/v1/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(RefreshRequest(refreshToken = expiredToken))
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should deny access to protected endpoint without token`() {
        mockMvc.get("/api/v1/agents")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `should reject request with invalid or expired token`() {
        mockMvc.get("/api/v1/agents") {
            header("Authorization", "Bearer invalid-token-garbage")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `should allow access to protected endpoint with valid token`() {
        val token = jwtTokenProvider.generateAccessToken("dashboard-client")

        mockMvc.get("/api/v1/agents") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `actuator health remains accessible without token`() {
        mockMvc.get("/actuator/health")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `auth endpoints are accessible without token`() {
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(AuthRequest(apiKey = "dev-api-key"))
        }.andExpect {
            status { isOk() }
        }
    }
}
