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

@WebMvcTest(CommandProxyController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class CommandProxyControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var proxyService: OrchestratorProxyService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `POST api-v1-proxy-commands-solve returns 200`() {
        whenever(proxyService.postSolve("""{"issue":1}""")).thenReturn("""{"id":"cmd-1"}""")

        mockMvc.post("/api/v1/proxy/commands/solve") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"issue":1}"""
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            content { json("""{"id":"cmd-1"}""") }
        }
    }

    @Test
    fun `POST api-v1-proxy-commands-init returns 200`() {
        whenever(proxyService.postInit("""{"project":"test"}""")).thenReturn("""{"ok":true}""")

        mockMvc.post("/api/v1/proxy/commands/init") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"project":"test"}"""
        }.andExpect {
            status { isOk() }
            content { json("""{"ok":true}""") }
        }
    }

    @Test
    fun `POST api-v1-proxy-commands-plan returns 200`() {
        whenever(proxyService.postPlan("""{"project":"test"}""")).thenReturn("""{"ok":true}""")

        mockMvc.post("/api/v1/proxy/commands/plan") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"project":"test"}"""
        }.andExpect {
            status { isOk() }
            content { json("""{"ok":true}""") }
        }
    }

    @Test
    fun `POST api-v1-proxy-commands-discuss returns 200`() {
        whenever(proxyService.postDiscuss("""{"msg":"hi"}""")).thenReturn("""{"reply":"hello"}""")

        mockMvc.post("/api/v1/proxy/commands/discuss") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"msg":"hi"}"""
        }.andExpect {
            status { isOk() }
            content { json("""{"reply":"hello"}""") }
        }
    }

    @Test
    fun `POST api-v1-proxy-commands-design returns 200`() {
        whenever(proxyService.postDesign("""{"spec":"v1"}""")).thenReturn("""{"design":"done"}""")

        mockMvc.post("/api/v1/proxy/commands/design") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"spec":"v1"}"""
        }.andExpect {
            status { isOk() }
            content { json("""{"design":"done"}""") }
        }
    }

    @Test
    fun `POST api-v1-proxy-commands-shell returns 200`() {
        whenever(proxyService.postShell("""{"cmd":"ls"}""")).thenReturn("""{"output":"files"}""")

        mockMvc.post("/api/v1/proxy/commands/shell") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"cmd":"ls"}"""
        }.andExpect {
            status { isOk() }
            content { json("""{"output":"files"}""") }
        }
    }
}
