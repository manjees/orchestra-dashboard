package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.AgentEventResponse
import com.orchestradashboard.server.model.CreateEventRequest
import com.orchestradashboard.server.service.EventService
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

@WebMvcTest(EventController::class)
@Import(SecurityConfig::class)
class EventControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var eventService: EventService

    private val sampleResponse =
        AgentEventResponse(
            id = "evt-1",
            agentId = "agent-1",
            type = "STATUS_CHANGE",
            payload = """{"from":"IDLE","to":"RUNNING"}""",
            timestamp = 1700000000L,
        )

    @Test
    fun `GET api-v1-events returns 200 with event list`() {
        whenever(eventService.getRecentEvents(null, 50)).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/events")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("evt-1") }
                jsonPath("$[0].type") { value("STATUS_CHANGE") }
            }
    }

    @Test
    fun `GET api-v1-events with agentId param returns filtered list`() {
        whenever(eventService.getRecentEvents("agent-1", 50)).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/events?agentId=agent-1")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].agent_id") { value("agent-1") }
            }
    }

    @Test
    fun `GET api-v1-events with limit param respects limit`() {
        whenever(eventService.getRecentEvents(null, 10)).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/events?limit=10")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `POST api-v1-events returns 201 on creation`() {
        val request = CreateEventRequest(agentId = "agent-1", type = "STATUS_CHANGE", payload = "{}")
        whenever(eventService.createEvent(request)).thenReturn(sampleResponse)

        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("evt-1") }
        }
    }

    @Test
    fun `POST api-v1-events returns 404 when agent not found`() {
        val request = CreateEventRequest(agentId = "missing", type = "ERROR")
        whenever(eventService.createEvent(request)).thenThrow(NoSuchElementException("Agent not found"))

        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
        }
    }
}
