package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.AgentRegistrationRequest
import com.orchestradashboard.server.model.AgentResponse
import com.orchestradashboard.server.model.HeartbeatRequest
import com.orchestradashboard.server.service.AgentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(AgentController::class)
@Import(SecurityConfig::class)
class AgentControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var agentService: AgentService

    private val sampleResponse =
        AgentResponse(
            id = "agent-1",
            name = "Worker Alpha",
            type = "WORKER",
            status = "RUNNING",
            lastHeartbeat = 1700000000L,
        )

    @Test
    fun `GET api-v1-agents returns 200 with agent list`() {
        whenever(agentService.getAllAgents()).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/agents")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("agent-1") }
                jsonPath("$[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `GET api-v1-agents with status param returns filtered list`() {
        whenever(agentService.getAgentsByStatus("RUNNING")).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/agents?status=RUNNING")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `GET api-v1-agents-id returns 200 when found`() {
        whenever(agentService.getAgent("agent-1")).thenReturn(sampleResponse)

        mockMvc.get("/api/v1/agents/agent-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Worker Alpha") }
            }
    }

    @Test
    fun `GET api-v1-agents-id returns 404 when not found`() {
        whenever(agentService.getAgent("missing")).thenThrow(NoSuchElementException("Agent not found"))

        mockMvc.get("/api/v1/agents/missing")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `POST api-v1-agents returns 201 on registration`() {
        val request = AgentRegistrationRequest(id = "agent-2", name = "Planner Bot", type = "PLANNER")
        val created = sampleResponse.copy(id = "agent-2", name = "Planner Bot", type = "PLANNER")
        whenever(agentService.registerAgent(request)).thenReturn(created)

        mockMvc.post("/api/v1/agents") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("agent-2") }
        }
    }

    @Test
    fun `POST api-v1-agents with no id assigns UUID`() {
        val request = AgentRegistrationRequest(name = "Auto Agent", type = "WORKER")
        val created = sampleResponse.copy(id = "auto-uuid", name = "Auto Agent")
        whenever(agentService.registerAgent(request)).thenReturn(created)

        mockMvc.post("/api/v1/agents") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("auto-uuid") }
        }
    }

    @Test
    fun `PUT api-v1-agents-id-heartbeat returns 200`() {
        val updated = sampleResponse.copy(status = "RUNNING", lastHeartbeat = 9999L)
        whenever(agentService.updateHeartbeat("agent-1", "RUNNING")).thenReturn(updated)

        mockMvc.put("/api/v1/agents/agent-1/heartbeat") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(HeartbeatRequest(status = "RUNNING"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("RUNNING") }
        }
    }

    @Test
    fun `PUT api-v1-agents-id-heartbeat returns 404 when not found`() {
        whenever(agentService.updateHeartbeat("missing", "RUNNING")).thenThrow(NoSuchElementException("Agent not found"))

        mockMvc.put("/api/v1/agents/missing/heartbeat") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(HeartbeatRequest(status = "RUNNING"))
        }.andExpect {
            status { isNotFound() }
        }
    }
}
