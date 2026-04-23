package com.orchestradashboard.shared.data.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class DeviceTokenResponseDto(
    val registeredAt: Long,
)
