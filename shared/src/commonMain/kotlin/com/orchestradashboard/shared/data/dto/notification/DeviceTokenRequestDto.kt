package com.orchestradashboard.shared.data.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenRequestDto(
    val token: String,
    val platform: String,
)
