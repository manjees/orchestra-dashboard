package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.repository.NotificationRepository

class GetNotificationSettingsUseCase(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(): NotificationSettings = repository.getNotificationSettings()
}
