package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.repository.NotificationRepository

class RegisterDeviceTokenUseCase(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(
        token: String,
        platform: DevicePlatform,
    ): Result<Unit> = repository.registerDeviceToken(token, platform)
}
