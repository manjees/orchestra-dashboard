package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.AgentEventResponse
import com.orchestradashboard.server.model.CreateEventRequest
import com.orchestradashboard.server.service.EventService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
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

@WebMvcTest(EventController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
@WithMockUser
class EventControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var eventService: EventService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    private val sampleResponse =
        AgentEventResponse(
            id = "evt-1",
            agentId = "agent-1",
            type = "STATUS_CHANGE",
            payload = mapOf("from" to "IDLE", "to" to "RUNNING"),
            timestamp = 1700000000L,
        )

    @Test
    fun `GET api-v1-events returns 200 with recent events`() {
        whenever(eventService.getRecentEvents(null)).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/events")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("evt-1") }
                jsonPath("$[0].agent_id") { value("agent-1") }
                jsonPath("$[0].type") { value("STATUS_CHANGE") }
                jsonPath("$[0].timestamp") { value(1700000000) }
            }
    }

    @Test
    fun `GET api-v1-events with agentId param filters by agent`() {
        whenever(eventService.getEventsByAgentId("agent-1", null)).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/events?agentId=agent-1")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].agent_id") { value("agent-1") }
            }
    }

    @Test
    fun `GET api-v1-events with limit param caps results`() {
        whenever(eventService.getRecentEvents(5)).thenReturn(emptyList())

        mockMvc.get("/api/v1/events?limit=5")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `GET api-v1-events with limit exceeding 100 is capped`() {
        whenever(eventService.getRecentEvents(200)).thenReturn(emptyList())

        mockMvc.get("/api/v1/events?limit=200")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `POST api-v1-events returns 201 on creation`() {
        val request = CreateEventRequest(agentId = "agent-1", type = "STATUS_CHANGE", payload = mapOf("from" to "IDLE"))
        whenever(eventService.createEvent(any())).thenReturn(sampleResponse)

        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("evt-1") }
            jsonPath("$.agent_id") { value("agent-1") }
        }
    }

    @Test
    fun `POST api-v1-events returns 404 when agent not found`() {
        whenever(eventService.createEvent(any())).thenThrow(NoSuchElementException("Agent not found"))

        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "invalid", "type": "STATUS_CHANGE"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    // --- Phase 2: /recent endpoint + validation ---

    @Test
    fun `GET api-v1-events-recent returns 200 with recent events`() {
        whenever(eventService.getRecentEvents(null)).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/events/recent")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value("evt-1") }
                jsonPath("$[0].agent_id") { value("agent-1") }
            }
    }

    @Test
    fun `GET api-v1-events-recent respects limit param`() {
        whenever(eventService.getRecentEvents(10)).thenReturn(emptyList())

        mockMvc.get("/api/v1/events/recent?limit=10")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `POST api-v1-events returns 400 when agent_id is blank`() {
        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "", "type": "STATUS_CHANGE"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.agentId") { value("agent_id must not be blank") }
        }
    }

    @Test
    fun `POST api-v1-events returns 400 when type is blank`() {
        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "type": ""}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.type") { value("type must not be blank") }
        }
    }

    @Test
    fun `POST api-v1-events returns 400 when type is invalid enum value`() {
        whenever(eventService.createEvent(any())).thenThrow(
            IllegalArgumentException(
                "Invalid event type 'INVALID'. Valid: HEARTBEAT, STATUS_CHANGE, PIPELINE_STARTED, PIPELINE_COMPLETED, ERROR",
            ),
        )

        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "type": "INVALID"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `POST api-v1-events with valid timestamp stores it`() {
        val responseWithTimestamp = sampleResponse.copy(timestamp = 1710000000000L)
        whenever(eventService.createEvent(any())).thenReturn(responseWithTimestamp)

        mockMvc.post("/api/v1/events") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "type": "STATUS_CHANGE", "timestamp": 1710000000000}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.timestamp") { value(1710000000000L) }
        }
    }
}
