package com.orchestradashboard.shared.domain.model

data class NotificationSettings(
    val enabled: Boolean = true,
    val notifyOnSuccess: Boolean = true,
    val notifyOnFailure: Boolean = true,
)
