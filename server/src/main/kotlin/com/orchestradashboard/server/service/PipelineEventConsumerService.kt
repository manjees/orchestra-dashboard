package com.orchestradashboard.server.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.model.notification.PipelineNotificationPayload
import com.orchestradashboard.server.service.notification.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient

@Service
class PipelineEventConsumerService(
    @Suppress("UnusedPrivateProperty") private val pipelineHistoryService: PipelineHistoryService,
    private val notificationService: NotificationService,
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
                "pipeline_started" -> logger.info("Pipeline event recorded: {}", eventType)
                "pipeline_completed", "pipeline_failed" -> {
                    logger.info("Pipeline event recorded: {}", eventType)
                    dispatchNotification(eventType, node)
                }
                else -> logger.debug("Ignoring event type: {}", eventType)
            }
        } catch (e: Exception) {
            logger.warn("Failed to process event: {}", e.message)
        }
    }

    private fun dispatchNotification(
        eventType: String,
        node: JsonNode,
    ) {
        val data = if (node.has("data")) node.path("data") else node
        val pipelineId = data.path("pipelineId").asText(null) ?: return
        val projectName = data.path("projectName").asText("")
        val issueNumber = data.path("issueNumber").takeIf { it.isInt }?.asInt()
        val prUrl = data.path("prUrl").asText(null)
        val status = if (eventType == "pipeline_completed") "success" else "failure"
        notificationService.dispatch(
            PipelineNotificationPayload(
                pipelineId = pipelineId,
                projectName = projectName,
                status = status,
                issueNumber = issueNumber,
                prUrl = prUrl,
            ),
        )
    }
}
