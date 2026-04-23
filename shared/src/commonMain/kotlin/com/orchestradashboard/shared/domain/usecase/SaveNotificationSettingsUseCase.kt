package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.repository.NotificationRepository

class SaveNotificationSettingsUseCase(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(settings: NotificationSettings) {
        repository.saveNotificationSettings(settings)
    }
}
