package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import com.orchestradashboard.shared.domain.repository.NotificationRepository
import com.orchestradashboard.shared.push.IOSNotificationLocalStore
import com.orchestradashboard.shared.push.IOSPushNotificationProvider
import kotlinx.coroutines.flow.Flow

/**
 * iOS-only stub [NotificationRepository]. Local settings persist via
 * [IOSNotificationLocalStore]; device-token registration is a no-op because
 * APNs integration is deferred (requires Apple Developer paid account —
 * tracked in a follow-up issue).
 */
class IOSNotificationRepositoryStub(
    private val localStore: IOSNotificationLocalStore = IOSNotificationLocalStore(),
    private val pushProvider: IOSPushNotificationProvider = IOSPushNotificationProvider(),
) : NotificationRepository {
    override suspend fun registerDeviceToken(
        token: String,
        platform: DevicePlatform,
    ): Result<Unit> = Result.success(Unit)

    override suspend fun unregisterDeviceToken(token: String): Result<Unit> = Result.success(Unit)

    override suspend fun getNotificationSettings(): NotificationSettings = localStore.load()

    override suspend fun saveNotificationSettings(settings: NotificationSettings) {
        localStore.save(settings)
    }

    override fun observeNotificationSettings(): Flow<NotificationSettings> = localStore.observe()

    override fun observeIncomingNotifications(): Flow<PushNotificationPayload> = pushProvider.incomingNotifications
}
