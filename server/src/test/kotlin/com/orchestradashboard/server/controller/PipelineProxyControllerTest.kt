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
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(PipelineProxyController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class PipelineProxyControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var proxyService: OrchestratorProxyService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `GET api-v1-orchestrator-pipelines returns 200 with list`() {
        whenever(proxyService.getPipelines()).thenReturn("""[{"id":"p1","status":"RUNNING"}]""")

        mockMvc.get("/api/v1/orchestrator/pipelines")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""[{"id":"p1","status":"RUNNING"}]""") }
            }
    }

    @Test
    fun `GET api-v1-orchestrator-pipelines-id returns 200 with detail`() {
        whenever(proxyService.getPipeline("p1")).thenReturn("""{"id":"p1","status":"PASSED"}""")

        mockMvc.get("/api/v1/orchestrator/pipelines/p1")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""{"id":"p1","status":"PASSED"}""") }
            }
    }

    @Test
    fun `GET api-v1-orchestrator-pipelines-history returns 200`() {
        whenever(proxyService.getPipelineHistory()).thenReturn("""[{"id":"h1"}]""")

        mockMvc.get("/api/v1/orchestrator/pipelines/history")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""[{"id":"h1"}]""") }
            }
    }

    @Test
    fun `GET api-v1-orchestrator-pipelines-parentId-parallel returns 200`() {
        whenever(proxyService.getParallelPipelines("p1")).thenReturn("""{"children":[]}""")

        mockMvc.get("/api/v1/orchestrator/pipelines/p1/parallel")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                content { json("""{"children":[]}""") }
            }
    }
}
