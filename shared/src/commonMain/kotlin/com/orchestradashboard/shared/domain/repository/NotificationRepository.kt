package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun registerDeviceToken(
        token: String,
        platform: DevicePlatform,
    ): Result<Unit>

    suspend fun unregisterDeviceToken(token: String): Result<Unit>

    suspend fun getNotificationSettings(): NotificationSettings

    suspend fun saveNotificationSettings(settings: NotificationSettings)

    fun observeNotificationSettings(): Flow<NotificationSettings>

    /** emits payloads received from the platform push provider */
    fun observeIncomingNotifications(): Flow<PushNotificationPayload>
}
