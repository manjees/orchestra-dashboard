package com.orchestradashboard.shared.domain.model

enum class NotificationStatus { SUCCESS, FAILURE }

data class PushNotificationPayload(
    val projectName: String,
    val pipelineId: String,
    val status: NotificationStatus,
    val timestamp: Long,
    val issueNumber: Int? = null,
    val prUrl: String? = null,
)
