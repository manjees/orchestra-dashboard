package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.service.OrchestratorProxyService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(CheckpointProxyController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class CheckpointProxyControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var proxyService: OrchestratorProxyService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `GET api-v1-checkpoints returns 200 with list`() {
        whenever(proxyService.getCheckpoints()).thenReturn("""[{"id":"cp-1","status":"PASSED"}]""")

        mockMvc.get("/api/v1/checkpoints")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""[{"id":"cp-1","status":"PASSED"}]""") }
            }
    }

    @Test
    fun `POST api-v1-checkpoints-id-retry returns 200`() {
        whenever(proxyService.retryCheckpoint("cp-1")).thenReturn("""{"id":"cp-1","status":"RETRYING"}""")

        mockMvc.post("/api/v1/checkpoints/cp-1/retry")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""{"id":"cp-1","status":"RETRYING"}""") }
            }
    }
}
