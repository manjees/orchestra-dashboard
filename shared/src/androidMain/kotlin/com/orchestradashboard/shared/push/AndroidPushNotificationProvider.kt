package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Android implementation of [PushNotificationProvider].
 *
 * The actual FCM token fetch is performed via reflection in the androidApp
 * `PushNotificationSetup` entry point so the shared module does not take a
 * hard dependency on `firebase-messaging`. Incoming payloads from the
 * `FirebaseMessagingService` are pushed into this provider's SharedFlow.
 *
 * Local notification display is delegated to the androidApp module
 * (`PushNotificationSetup.showNotification`) by invoking the optional
 * [localNotificationHandler] callback registered at app init.
 */
class AndroidPushNotificationProvider : PushNotificationProvider {
    override val platform: DevicePlatform = DevicePlatform.ANDROID

    private val incoming =
        MutableSharedFlow<PushNotificationPayload>(
            replay = 0,
            extraBufferCapacity = 16,
        )

    override val incomingNotifications: Flow<PushNotificationPayload> = incoming.asSharedFlow()

    /** Set by androidApp during initialization to bridge into FCM token retrieval. */
    var tokenFetcher: (suspend () -> String?)? = null

    /** Set by androidApp during initialization to display local notifications. */
    var localNotificationHandler: ((PushNotificationPayload) -> Unit)? = null

    override suspend fun requestToken(): String? = tokenFetcher?.invoke()

    override fun showLocalNotification(payload: PushNotificationPayload) {
        localNotificationHandler?.invoke(payload)
    }

    /** Called by Android FCM service when a push arrives. */
    fun emitIncoming(payload: PushNotificationPayload) {
        incoming.tryEmit(payload)
    }
}
