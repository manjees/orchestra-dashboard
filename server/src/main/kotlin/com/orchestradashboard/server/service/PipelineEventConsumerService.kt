package com.orchestradashboard.server.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient

@Service
class PipelineEventConsumerService(
    @Suppress("UnusedPrivateProperty") private val pipelineHistoryService: PipelineHistoryService,
    @Value("\${dashboard.orchestrator.api-url}") private val orchestratorUrl: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = ObjectMapper()

    @EventListener(ApplicationReadyEvent::class)
    fun startConsuming() {
        val wsUrl = orchestratorUrl.replace("http://", "ws://").replace("https://", "wss://")
        val client = ReactorNettyWebSocketClient()
        val uri = java.net.URI.create("$wsUrl/ws/events")

        client.execute(uri) { session ->
            session.receive()
                .map { it.payloadAsText }
                .doOnNext { processEvent(it) }
                .doOnError { logger.warn("WebSocket error, will reconnect: {}", it.message) }
                .then()
        }.doOnError {
            logger.warn("WebSocket connection lost: {}", it.message)
        }.retry().subscribe()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun processEvent(payload: String) {
        try {
            val node: JsonNode = objectMapper.readTree(payload)
            val eventType = node.path("event").asText()
            logger.debug("Received orchestrator event: {}", eventType)

            when (eventType) {
                "pipeline_started", "pipeline_completed", "pipeline_failed" -> {
                    logger.info("Pipeline event recorded: {}", eventType)
                }
                else -> logger.debug("Ignoring event type: {}", eventType)
            }
        } catch (e: Exception) {
            logger.warn("Failed to process event: {}", e.message)
        }
    }
}
