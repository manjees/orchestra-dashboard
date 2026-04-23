package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * iOS implementation of [PushNotificationProvider].
 *
 * TODO: APNs integration deferred — requires Apple Developer paid account for
 * Push Notifications capability. Remote push token retrieval and registration
 * will be added in a follow-up issue. For now:
 * - `requestToken()` returns `null`
 * - local `UNUserNotificationCenter` notifications should be triggered from
 *   the Swift side via [emitIncoming] or by calling `showLocalNotification`
 *   which delegates to a Swift-registered handler.
 */
class IOSPushNotificationProvider : PushNotificationProvider {
    override val platform: DevicePlatform = DevicePlatform.IOS

    private val incoming =
        MutableSharedFlow<PushNotificationPayload>(
            replay = 0,
            extraBufferCapacity = 16,
        )

    override val incomingNotifications: Flow<PushNotificationPayload> = incoming.asSharedFlow()

    /** Set by the Swift side to display local UNUserNotificationCenter notifications. */
    var localNotificationHandler: ((PushNotificationPayload) -> Unit)? = null

    // TODO: APNs — requires Apple Developer paid account. Track in separate issue.
    override suspend fun requestToken(): String? = null

    override fun showLocalNotification(payload: PushNotificationPayload) {
        localNotificationHandler?.invoke(payload)
    }

    /** Called by Swift side when an incoming notification is received. */
    fun emitIncoming(payload: PushNotificationPayload) {
        incoming.tryEmit(payload)
    }
}
