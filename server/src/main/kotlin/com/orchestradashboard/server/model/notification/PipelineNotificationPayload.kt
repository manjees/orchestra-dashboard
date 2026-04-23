package com.orchestradashboard.server.model.notification

/**
 * Event-driven payload carried from the orchestrator WebSocket to
 * [com.orchestradashboard.server.service.notification.NotificationService].
 */
data class PipelineNotificationPayload(
    val pipelineId: String,
    val projectName: String,
    val status: String,
    val issueNumber: Int? = null,
    val prUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)
