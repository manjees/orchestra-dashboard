package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.AgentResponse
import com.orchestradashboard.server.model.CreateAgentRequest
import com.orchestradashboard.server.service.AgentService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
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
    fun `GET api-v1-agents-id returns 200 when agent exists`() {
        whenever(agentService.getAgent("agent-1")).thenReturn(sampleResponse)

        mockMvc.get("/api/v1/agents/agent-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.name") { value("Worker Alpha") }
            }
    }

    @Test
    fun `GET api-v1-agents-id returns 404 when agent not found`() {
        whenever(agentService.getAgent("missing")).thenReturn(null)

        mockMvc.get("/api/v1/agents/missing")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `POST api-v1-agents returns 201 on successful registration`() {
        val request = CreateAgentRequest(id = "agent-2", name = "Planner Bot", type = "PLANNER")
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
    fun `DELETE api-v1-agents-id returns 204 when agent is deregistered`() {
        whenever(agentService.deregisterAgent("agent-1")).thenReturn(true)

        mockMvc.delete("/api/v1/agents/agent-1")
            .andExpect {
                status { isNoContent() }
            }
    }

    @Test
    fun `DELETE api-v1-agents-id returns 404 when agent not found`() {
        whenever(agentService.deregisterAgent("missing")).thenReturn(false)

        mockMvc.delete("/api/v1/agents/missing")
            .andExpect {
                status { isNotFound() }
            }
    }
}
