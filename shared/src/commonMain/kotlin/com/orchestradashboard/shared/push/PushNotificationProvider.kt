package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import kotlinx.coroutines.flow.Flow

/**
 * Platform abstraction for push notification providers.
 *
 * Each platform (Android/Desktop/iOS) supplies its own implementation.
 * - Android: FCM + NotificationCompat
 * - Desktop: java.awt.SystemTray TrayIcon
 * - iOS: UNUserNotificationCenter (local only; APNs stubbed)
 */
interface PushNotificationProvider {
    val platform: DevicePlatform

    /**
     * Returns the device push token, or null if the platform does not support
     * remote push (e.g. desktop) or the provider is unavailable (e.g. FCM init failed).
     */
    suspend fun requestToken(): String?

    /** Flow of payloads emitted when the platform layer receives a push. */
    val incomingNotifications: Flow<PushNotificationPayload>

    /** Display a local notification for the supplied payload. */
    fun showLocalNotification(payload: PushNotificationPayload)
}
