package com.orchestradashboard.shared.ui.settings

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.NotificationSettings
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import com.orchestradashboard.shared.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeNotificationRepository : NotificationRepository {
    var registerResult: Result<Unit> = Result.success(Unit)
    var unregisterResult: Result<Unit> = Result.success(Unit)

    var lastRegisteredToken: String? = null
        private set
    var lastRegisteredPlatform: DevicePlatform? = null
        private set
    var lastUnregisteredToken: String? = null
        private set
    var saveCallCount: Int = 0
        private set

    private val settingsFlow = MutableStateFlow(NotificationSettings())
    private val incoming = MutableSharedFlow<PushNotificationPayload>(extraBufferCapacity = 8)

    var currentSettings: NotificationSettings
        get() = settingsFlow.value
        set(value) {
            settingsFlow.value = value
        }

    override suspend fun registerDeviceToken(
        token: String,
        platform: DevicePlatform,
    ): Result<Unit> {
        lastRegisteredToken = token
        lastRegisteredPlatform = platform
        return registerResult
    }

    override suspend fun unregisterDeviceToken(token: String): Result<Unit> {
        lastUnregisteredToken = token
        return unregisterResult
    }

    override suspend fun getNotificationSettings(): NotificationSettings = settingsFlow.value

    override suspend fun saveNotificationSettings(settings: NotificationSettings) {
        saveCallCount++
        settingsFlow.value = settings
    }

    override fun observeNotificationSettings(): Flow<NotificationSettings> = settingsFlow.asStateFlow()

    override fun observeIncomingNotifications(): Flow<PushNotificationPayload> = incoming.asSharedFlow()

    fun emitIncoming(payload: PushNotificationPayload) {
        incoming.tryEmit(payload)
    }
}
