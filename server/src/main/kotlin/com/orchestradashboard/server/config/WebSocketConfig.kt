package com.orchestradashboard.server.config

import com.orchestradashboard.server.websocket.AgentEventWebSocketHandler
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val agentEventWebSocketHandler: AgentEventWebSocketHandler,
) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        val allowedOrigins =
            System.getenv("ALLOWED_ORIGINS")
                ?.split(",")
                ?.toTypedArray()
                ?: arrayOf("http://localhost:*")

        registry
            .addHandler(agentEventWebSocketHandler, "/ws/events")
            .setAllowedOriginPatterns(*allowedOrigins)
    }
}
