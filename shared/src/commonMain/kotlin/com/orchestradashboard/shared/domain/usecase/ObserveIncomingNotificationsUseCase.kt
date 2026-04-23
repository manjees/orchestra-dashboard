package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import com.orchestradashboard.shared.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow

class ObserveIncomingNotificationsUseCase(
    private val repository: NotificationRepository,
) {
    operator fun invoke(): Flow<PushNotificationPayload> = repository.observeIncomingNotifications()
}
