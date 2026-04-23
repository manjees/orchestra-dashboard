package com.orchestradashboard.server.model.notification

data class NotificationDispatchResult(
    val attempted: Int,
    val succeeded: Int,
    val failed: Int,
)
