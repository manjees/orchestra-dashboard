package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.AgentCommandResponse
import com.orchestradashboard.server.service.CommandService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(CommandController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class CommandControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var commandService: CommandService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    private val sampleResponse =
        AgentCommandResponse(
            id = "cmd-1",
            agentId = "agent-1",
            commandType = "STOP",
            status = "PENDING",
            requestedAt = 1700000000L,
            requestedBy = "user",
        )

    @Test
    fun `should reject unauthorized command requests`() {
        mockMvc.post("/api/v1/commands") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "command_type": "STOP"}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    @WithMockUser
    fun `should reject command with missing fields`() {
        mockMvc.post("/api/v1/commands") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "", "command_type": ""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    @WithMockUser
    fun `should reject command for nonexistent agent`() {
        whenever(commandService.createCommand(any(), any()))
            .thenThrow(NoSuchElementException("Agent 'nonexistent' not found"))

        mockMvc.post("/api/v1/commands") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "nonexistent", "command_type": "STOP"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @WithMockUser(username = "dashboard-client")
    fun `should create command and return 201 with valid request`() {
        whenever(commandService.createCommand(any(), eq("dashboard-client")))
            .thenReturn(sampleResponse)

        mockMvc.post("/api/v1/commands") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "command_type": "STOP"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("cmd-1") }
            jsonPath("$.agent_id") { value("agent-1") }
            jsonPath("$.command_type") { value("STOP") }
            jsonPath("$.status") { value("PENDING") }
            jsonPath("$.requested_at") { value(1700000000) }
            jsonPath("$.requested_by") { value("user") }
        }
    }

    @Test
    @WithMockUser
    fun `should reject command when active command already exists for agent`() {
        whenever(commandService.createCommand(any(), any()))
            .thenThrow(IllegalStateException("Agent 'agent-1' already has an active command (PENDING)"))

        mockMvc.post("/api/v1/commands") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "command_type": "STOP"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    @WithMockUser
    fun `should return command history for an agent`() {
        whenever(commandService.getCommandsByAgentId("agent-1", null))
            .thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/commands?agentId=agent-1")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value("cmd-1") }
                jsonPath("$[0].agent_id") { value("agent-1") }
            }
    }

    @Test
    @WithMockUser
    fun `should reject invalid command type`() {
        whenever(commandService.createCommand(any(), any()))
            .thenThrow(IllegalArgumentException("Invalid command type 'INVALID'. Valid: [START, STOP, RESTART]"))

        mockMvc.post("/api/v1/commands") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "command_type": "INVALID"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { exists() }
        }
    }
}
