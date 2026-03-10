package com.orchestradashboard.server.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.model.AgentEventResponse
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Component
class AgentEventWebSocketHandler(
    private val objectMapper: ObjectMapper,
) : TextWebSocketHandler() {
    private val log = LoggerFactory.getLogger(javaClass)

    private data class SessionEntry(
        val session: WebSocketSession,
        val agentIdFilter: String?,
    )

    private val sessions = ConcurrentHashMap<String, SessionEntry>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val agentIdFilter = extractAgentIdFilter(session)
        sessions[session.id] = SessionEntry(session, agentIdFilter)
        log.info("WebSocket connected: {} (filter={})", session.id, agentIdFilter ?: "all")
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        sessions.remove(session.id)
        log.info("WebSocket disconnected: {} (status={})", session.id, status)
    }

    override fun handleTransportError(
        session: WebSocketSession,
        exception: Throwable,
    ) {
        sessions.remove(session.id)
        log.warn("WebSocket transport error for {}: {}", session.id, exception.message)
    }

    fun broadcastEvent(event: AgentEventResponse) {
        val message = WebSocketMessage(type = "AGENT_EVENT", data = event)
        val json = TextMessage(objectMapper.writeValueAsString(message))

        val iterator = sessions.entries.iterator()
        while (iterator.hasNext()) {
            val (_, entry) = iterator.next()
            if (entry.agentIdFilter != null && entry.agentIdFilter != event.agentId) {
                continue
            }
            try {
                entry.session.sendMessage(json)
            } catch (e: IOException) {
                log.warn("Failed to send to session {}, removing: {}", entry.session.id, e.message)
                iterator.remove()
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        sessions.values.forEach { entry ->
            try {
                entry.session.close(CloseStatus.GOING_AWAY)
            } catch (_: IOException) {
                // Session already closed
            }
        }
        sessions.clear()
    }

    private fun extractAgentIdFilter(session: WebSocketSession): String? {
        val query = session.uri?.query ?: return null
        val value =
            query
                .split("&")
                .map { it.split("=", limit = 2) }
                .firstOrNull { it.size == 2 && it[0] == "agentId" }
                ?.get(1)
                ?: return null

        // Only allow alphanumeric, hyphens, and underscores to prevent injection
        if (!value.matches(AGENT_ID_PATTERN)) {
            log.warn("Rejected invalid agentId filter: {}", value)
            return null
        }
        return value
    }

    companion object {
        private val AGENT_ID_PATTERN = Regex("^[a-zA-Z0-9_-]+$")
    }
}
