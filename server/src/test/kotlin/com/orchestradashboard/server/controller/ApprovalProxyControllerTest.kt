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
import org.springframework.test.web.servlet.post
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(ApprovalProxyController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class ApprovalProxyControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var proxyService: OrchestratorProxyService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `POST api-v1-approvals-id-respond returns 200`() {
        whenever(proxyService.respondToApproval("appr-1", """{"approved":true}"""))
            .thenReturn("""{"status":"approved"}""")

        mockMvc.post("/api/v1/approvals/appr-1/respond") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"approved":true}"""
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json("""{"status":"approved"}""") }
        }
    }

    @Test
    fun `POST api-v1-approvals-id-respond with rejection`() {
        whenever(proxyService.respondToApproval("appr-2", """{"approved":false,"reason":"not ready"}"""))
            .thenReturn("""{"status":"rejected"}""")

        mockMvc.post("/api/v1/approvals/appr-2/respond") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"approved":false,"reason":"not ready"}"""
        }.andExpect {
            status { isOk() }
            content { json("""{"status":"rejected"}""") }
        }
    }
}
