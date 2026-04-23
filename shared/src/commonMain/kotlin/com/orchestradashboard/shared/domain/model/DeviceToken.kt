package com.orchestradashboard.shared.domain.model

enum class DevicePlatform { ANDROID, IOS, DESKTOP }

data class DeviceToken(
    val token: String,
    val platform: DevicePlatform,
    val createdAt: Long,
)
