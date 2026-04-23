package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.notification.DeviceTokenRequest
import com.orchestradashboard.server.model.notification.DeviceTokenResponse
import com.orchestradashboard.server.service.notification.NotificationService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(NotificationController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class NotificationControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var notificationService: NotificationService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `POST devices returns 201 Created with registeredAt`() {
        whenever(notificationService.registerToken(any<DeviceTokenRequest>()))
            .thenReturn(DeviceTokenResponse(registeredAt = 1_700_000_000L))

        mockMvc.post("/api/v1/notifications/devices") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"tok-abc","platform":"ANDROID"}"""
        }.andExpect {
            status { isCreated() }
            content { json("""{"registeredAt":1700000000}""") }
        }
    }

    @Test
    fun `POST devices with blank token returns 400`() {
        mockMvc.post("/api/v1/notifications/devices") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"","platform":"ANDROID"}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `POST devices with missing platform returns 400`() {
        mockMvc.post("/api/v1/notifications/devices") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"tok-abc","platform":""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `DELETE devices returns 204 when token removed`() {
        whenever(notificationService.unregisterToken("tok-1")).thenReturn(true)

        mockMvc.delete("/api/v1/notifications/devices/tok-1")
            .andExpect { status { isNoContent() } }
    }

    @Test
    fun `DELETE devices returns 404 when token not found`() {
        whenever(notificationService.unregisterToken("missing")).thenReturn(false)

        mockMvc.delete("/api/v1/notifications/devices/missing")
            .andExpect { status { isNotFound() } }
    }
}
