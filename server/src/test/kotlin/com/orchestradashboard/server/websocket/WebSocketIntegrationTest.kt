package com.orchestradashboard.server.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.repository.AgentEventJpaRepository
import com.orchestradashboard.server.repository.AgentJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.URI
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var agentRepository: AgentJpaRepository

    @Autowired
    private lateinit var eventRepository: AgentEventJpaRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
        eventRepository.deleteAll()
        agentRepository.deleteAll()
        agentRepository.save(
            AgentEntity(
                id = "agent-1",
                name = "TestAgent",
                type = "WORKER",
                status = "RUNNING",
                lastHeartbeat = System.currentTimeMillis(),
            ),
        )
    }

    private fun connectWebSocket(path: String = "/ws/events"): Pair<WebSocketSession, ArrayBlockingQueue<String>> {
        val messages = ArrayBlockingQueue<String>(10)
        val client = StandardWebSocketClient()
        val handler =
            object : TextWebSocketHandler() {
                override fun handleTextMessage(
                    session: WebSocketSession,
                    message: TextMessage,
                ) {
                    messages.add(message.payload)
                }
            }
        val session =
            client
                .execute(handler, WebSocketHttpHeaders(), URI("ws://localhost:$port$path"))
                .get(5, TimeUnit.SECONDS)
        return Pair(session, messages)
    }

    @Test
    fun `should connect to WebSocket endpoint`() {
        val (session, _) = connectWebSocket()
        try {
            assertTrue(session.isOpen)
        } finally {
            session.close()
        }
    }

    @Test
    fun `should receive event after POST to events API`() {
        val (session, messages) = connectWebSocket()
        try {
            // Allow connection to fully establish
            Thread.sleep(200)

            mockMvc
                .post("/api/v1/events") {
                    contentType = MediaType.APPLICATION_JSON
                    content =
                        """{"agent_id": "agent-1", "type": "STATUS_CHANGE", "payload": {"from": "IDLE"}}"""
                }.andExpect { status { isCreated() } }

            val received = messages.poll(5, TimeUnit.SECONDS)
            assertNotNull(received, "Should have received a WebSocket message")

            val message = objectMapper.readTree(received)
            assertEquals("AGENT_EVENT", message["type"].asText())
            assertEquals("agent-1", message["data"]["agent_id"].asText())
            assertEquals("STATUS_CHANGE", message["data"]["type"].asText())
        } finally {
            session.close()
        }
    }

    @Test
    fun `should filter events by agentId subscription`() {
        agentRepository.save(
            AgentEntity(
                id = "agent-2",
                name = "OtherAgent",
                type = "WORKER",
                status = "RUNNING",
                lastHeartbeat = System.currentTimeMillis(),
            ),
        )

        val (session, messages) = connectWebSocket("/ws/events?agentId=agent-1")
        try {
            Thread.sleep(200)

            // Post event for agent-2 (should NOT be received)
            mockMvc
                .post("/api/v1/events") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"agent_id": "agent-2", "type": "HEARTBEAT"}"""
                }.andExpect { status { isCreated() } }

            // Post event for agent-1 (should be received)
            mockMvc
                .post("/api/v1/events") {
                    contentType = MediaType.APPLICATION_JSON
                    content = """{"agent_id": "agent-1", "type": "STATUS_CHANGE"}"""
                }.andExpect { status { isCreated() } }

            val received = messages.poll(5, TimeUnit.SECONDS)
            assertNotNull(received, "Should have received the agent-1 event")

            val message = objectMapper.readTree(received)
            assertEquals("agent-1", message["data"]["agent_id"].asText())

            // Verify no more messages (agent-2 event was filtered)
            val extra = messages.poll(1, TimeUnit.SECONDS)
            assertTrue(extra == null, "Should not have received agent-2 event")
        } finally {
            session.close()
        }
    }

    @Test
    fun `should handle disconnect cleanup gracefully`() {
        val (session, _) = connectWebSocket()
        assertTrue(session.isOpen)
        session.close()

        Thread.sleep(200)

        // Posting an event after disconnect should not cause errors
        mockMvc
            .post("/api/v1/events") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"agent_id": "agent-1", "type": "HEARTBEAT"}"""
            }.andExpect { status { isCreated() } }
    }
}
