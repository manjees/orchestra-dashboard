package com.orchestradashboard.server.model.notification

data class DeviceTokenRecord(
    val token: String,
    val platform: String,
    val createdAt: Long,
)
