package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakePushNotificationProvider(
    override val platform: DevicePlatform = DevicePlatform.ANDROID,
) : PushNotificationProvider {
    private val incoming = MutableSharedFlow<PushNotificationPayload>(extraBufferCapacity = 8)

    var tokenToReturn: String? = "fake-token"
    var shownNotifications: MutableList<PushNotificationPayload> = mutableListOf()

    override val incomingNotifications: Flow<PushNotificationPayload> = incoming.asSharedFlow()

    override suspend fun requestToken(): String? = tokenToReturn

    override fun showLocalNotification(payload: PushNotificationPayload) {
        shownNotifications += payload
    }

    fun emitIncoming(payload: PushNotificationPayload) {
        incoming.tryEmit(payload)
    }
}
