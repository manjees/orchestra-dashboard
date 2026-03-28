package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.AgentResponse
import com.orchestradashboard.server.model.PagedAgentResponse
import com.orchestradashboard.server.service.AgentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(AgentController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
@WithMockUser
class AgentPaginationControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var agentService: AgentService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    private val sampleResponse =
        AgentResponse(
            id = "agent-1",
            name = "Worker Alpha",
            type = "WORKER",
            status = "RUNNING",
            lastHeartbeat = 1700000000L,
        )

    @Test
    fun `should update pagination metadata correctly`() {
        whenever(agentService.getAgentsPaged(0, 10, null)).thenReturn(
            PagedAgentResponse(
                content = listOf(sampleResponse),
                page = 0,
                pageSize = 10,
                totalElements = 25,
                totalPages = 3,
            ),
        )

        mockMvc.get("/api/v1/agents/paged?page=0&pageSize=10")
            .andExpect {
                status { isOk() }
                jsonPath("$.page") { value(0) }
                jsonPath("$.pageSize") { value(10) }
                jsonPath("$.totalElements") { value(25) }
                jsonPath("$.totalPages") { value(3) }
                jsonPath("$.content[0].id") { value("agent-1") }
            }
    }

    @Test
    fun `should combine status filter with pagination`() {
        whenever(agentService.getAgentsPaged(0, 10, "RUNNING")).thenReturn(
            PagedAgentResponse(
                content = listOf(sampleResponse),
                page = 0,
                pageSize = 10,
                totalElements = 1,
                totalPages = 1,
            ),
        )

        mockMvc.get("/api/v1/agents/paged?page=0&pageSize=10&status=RUNNING")
            .andExpect {
                status { isOk() }
                jsonPath("$.totalElements") { value(1) }
                jsonPath("$.content[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `should reject pageSize exceeding maximum`() {
        mockMvc.get("/api/v1/agents/paged?page=0&pageSize=200")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should reject negative page number`() {
        mockMvc.get("/api/v1/agents/paged?page=-1&pageSize=10")
            .andExpect {
                status { isBadRequest() }
            }
    }

    @Test
    fun `should use default page and pageSize when not provided`() {
        whenever(agentService.getAgentsPaged(0, 20, null)).thenReturn(
            PagedAgentResponse(
                content = emptyList(),
                page = 0,
                pageSize = 20,
                totalElements = 0,
                totalPages = 0,
            ),
        )

        mockMvc.get("/api/v1/agents/paged")
            .andExpect {
                status { isOk() }
                jsonPath("$.page") { value(0) }
                jsonPath("$.pageSize") { value(20) }
            }
    }
}
