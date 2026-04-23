package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.NotificationMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import com.orchestradashboard.shared.domain.repository.NotificationRepository
import com.orchestradashboard.shared.push.NotificationLocalStore
import com.orchestradashboard.shared.push.PushNotificationProvider
import kotlinx.coroutines.flow.Flow

class NotificationRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: NotificationMapper,
    private val localStore: NotificationLocalStore,
    private val pushProvider: PushNotificationProvider,
) : NotificationRepository {
    override suspend fun registerDeviceToken(
        token: String,
        platform: DevicePlatform,
    ): Result<Unit> =
        runCatching {
            val request = mapper.toDto(token, platform)
            api.registerDeviceToken(request)
            Unit
        }

    override suspend fun unregisterDeviceToken(token: String): Result<Unit> =
        runCatching {
            api.unregisterDeviceToken(token)
        }

    override suspend fun getNotificationSettings(): NotificationSettings = localStore.load()

    override suspend fun saveNotificationSettings(settings: NotificationSettings) {
        localStore.save(settings)
    }

    override fun observeNotificationSettings(): Flow<NotificationSettings> = localStore.observe()

    override fun observeIncomingNotifications(): Flow<PushNotificationPayload> = pushProvider.incomingNotifications
}
