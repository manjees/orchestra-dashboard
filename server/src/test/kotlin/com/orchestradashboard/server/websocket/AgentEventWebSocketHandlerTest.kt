package com.orchestradashboard.server.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.orchestradashboard.server.model.AgentEventResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import java.net.URI

class AgentEventWebSocketHandlerTest {
    private val objectMapper = jacksonObjectMapper()
    private lateinit var handler: AgentEventWebSocketHandler

    @BeforeEach
    fun setUp() {
        handler = AgentEventWebSocketHandler(objectMapper)
    }

    private fun mockSession(
        id: String = "session-1",
        uri: URI = URI("/ws/events"),
        open: Boolean = true,
    ): WebSocketSession {
        val session = mock<WebSocketSession>()
        whenever(session.id).thenReturn(id)
        whenever(session.uri).thenReturn(uri)
        whenever(session.isOpen).thenReturn(open)
        return session
    }

    private val sampleEvent =
        AgentEventResponse(
            id = "evt-1",
            agentId = "agent-1",
            type = "STATUS_CHANGE",
            payload = mapOf("from" to "IDLE", "to" to "RUNNING"),
            timestamp = 1700000000L,
        )

    @Test
    fun `should register session and deliver broadcast messages`() {
        val session = mockSession()

        handler.afterConnectionEstablished(session)

        handler.broadcastEvent(sampleEvent)
        verify(session).sendMessage(any<TextMessage>())
    }

    @Test
    fun `should not deliver messages after session is closed`() {
        val session = mockSession()
        handler.afterConnectionEstablished(session)

        handler.afterConnectionClosed(session, CloseStatus.NORMAL)

        handler.broadcastEvent(sampleEvent)
        verify(session, never()).sendMessage(any<TextMessage>())
    }

    @Test
    fun `should broadcast event to all connected sessions`() {
        val session1 = mockSession(id = "session-1")
        val session2 = mockSession(id = "session-2")
        handler.afterConnectionEstablished(session1)
        handler.afterConnectionEstablished(session2)

        handler.broadcastEvent(sampleEvent)

        verify(session1).sendMessage(any<TextMessage>())
        verify(session2).sendMessage(any<TextMessage>())
    }

    @Test
    fun `should only deliver events matching the subscribed agentId filter`() {
        val subscribedSession = mockSession(id = "s1", uri = URI("/ws/events?agentId=agent-1"))
        val otherSession = mockSession(id = "s2", uri = URI("/ws/events?agentId=agent-2"))
        handler.afterConnectionEstablished(subscribedSession)
        handler.afterConnectionEstablished(otherSession)

        handler.broadcastEvent(sampleEvent)

        verify(subscribedSession).sendMessage(any<TextMessage>())
        verify(otherSession, never()).sendMessage(any<TextMessage>())
    }

    @Test
    fun `should deliver all events to sessions without agentId filter`() {
        val unfilteredSession = mockSession(id = "s1", uri = URI("/ws/events"))
        handler.afterConnectionEstablished(unfilteredSession)

        handler.broadcastEvent(sampleEvent)

        verify(unfilteredSession).sendMessage(any<TextMessage>())
    }

    @Test
    fun `should remove dead sessions when send fails with IOException`() {
        val deadSession = mockSession(id = "dead")
        whenever(deadSession.sendMessage(any<TextMessage>())).thenThrow(java.io.IOException("Connection reset"))
        handler.afterConnectionEstablished(deadSession)

        handler.broadcastEvent(sampleEvent)

        val aliveSession = mockSession(id = "alive")
        handler.afterConnectionEstablished(aliveSession)
        handler.broadcastEvent(sampleEvent)
        verify(deadSession).sendMessage(any<TextMessage>())
        verify(aliveSession).sendMessage(any<TextMessage>())
    }

    @Test
    fun `should remove session on transport error`() {
        val session = mockSession()
        handler.afterConnectionEstablished(session)

        handler.handleTransportError(session, RuntimeException("Transport error"))

        handler.broadcastEvent(sampleEvent)
        verify(session, never()).sendMessage(any<TextMessage>())
    }

    @Test
    fun `should serialize message as AGENT_EVENT envelope with snake_case fields`() {
        val session = mockSession()
        handler.afterConnectionEstablished(session)

        handler.broadcastEvent(sampleEvent)

        val captor = argumentCaptor<org.springframework.web.socket.WebSocketMessage<*>>()
        verify(session).sendMessage(captor.capture())
        val textMessage = captor.firstValue as TextMessage
        val message = objectMapper.readTree(textMessage.payload)
        assertEquals("AGENT_EVENT", message["type"].asText())
        assertTrue(message.has("data"))
        assertEquals("evt-1", message["data"]["id"].asText())
        assertEquals("agent-1", message["data"]["agent_id"].asText())
        assertEquals("STATUS_CHANGE", message["data"]["type"].asText())
        assertEquals(1700000000L, message["data"]["timestamp"].asLong())
    }

    @Test
    fun `should reject agentId filter containing special characters`() {
        val maliciousSession =
            mockSession(
                id = "s-bad",
                uri = URI("/ws/events?agentId=agent%3B%20DROP%20TABLE"),
            )
        handler.afterConnectionEstablished(maliciousSession)

        handler.broadcastEvent(sampleEvent)

        verify(maliciousSession).sendMessage(any<TextMessage>())
    }

    @Test
    fun `should accept agentId filter with valid characters`() {
        val session = mockSession(id = "s-valid", uri = URI("/ws/events?agentId=agent-1_test"))
        handler.afterConnectionEstablished(session)

        val event = sampleEvent.copy(agentId = "agent-1_test")
        handler.broadcastEvent(event)

        verify(session).sendMessage(any<TextMessage>())
    }
}
