package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.notification.DeviceTokenRequestDto
import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.DeviceToken

class NotificationMapper {
    fun toDto(deviceToken: DeviceToken): DeviceTokenRequestDto =
        DeviceTokenRequestDto(
            token = deviceToken.token,
            platform = deviceToken.platform.name,
        )

    fun toDto(
        token: String,
        platform: DevicePlatform,
    ): DeviceTokenRequestDto =
        DeviceTokenRequestDto(
            token = token,
            platform = platform.name,
        )
}
