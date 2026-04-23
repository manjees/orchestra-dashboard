package com.orchestradashboard.server.model.notification

import jakarta.validation.constraints.NotBlank

data class DeviceTokenRequest(
    @field:NotBlank
    val token: String,
    @field:NotBlank
    val platform: String,
)
