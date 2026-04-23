package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.NotificationStatus
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.image.BufferedImage

/**
 * Desktop implementation using [java.awt.SystemTray]. Falls back to no-op on
 * headless environments (CI, servers without display).
 */
class DesktopPushNotificationProvider : PushNotificationProvider {
    override val platform: DevicePlatform = DevicePlatform.DESKTOP

    private val incoming =
        MutableSharedFlow<PushNotificationPayload>(
            replay = 0,
            extraBufferCapacity = 16,
        )

    override val incomingNotifications: Flow<PushNotificationPayload> = incoming.asSharedFlow()

    private var trayIcon: TrayIcon? = null
    private var actionListener: ((PushNotificationPayload) -> Unit)? = null
    private var lastPayload: PushNotificationPayload? = null

    /** Desktop clients do not register remote push tokens. */
    override suspend fun requestToken(): String? = null

    override fun showLocalNotification(payload: PushNotificationPayload) {
        lastPayload = payload
        val tray = ensureTrayIcon() ?: return
        val title =
            when (payload.status) {
                NotificationStatus.SUCCESS -> "Pipeline Completed"
                NotificationStatus.FAILURE -> "Pipeline Failed"
            }
        val message =
            buildString {
                append(payload.projectName)
                payload.issueNumber?.let { append(" #").append(it) }
                append(" (").append(payload.pipelineId).append(")")
            }
        val messageType =
            when (payload.status) {
                NotificationStatus.SUCCESS -> TrayIcon.MessageType.INFO
                NotificationStatus.FAILURE -> TrayIcon.MessageType.ERROR
            }
        tray.displayMessage(title, message, messageType)
    }

    /** Hook invoked when user clicks the tray notification/icon. */
    fun setActionListener(listener: (PushNotificationPayload) -> Unit) {
        actionListener = listener
    }

    /** Called externally (e.g. from WebSocket event stream) to trigger the incoming flow. */
    fun emitIncoming(payload: PushNotificationPayload) {
        incoming.tryEmit(payload)
    }

    private fun ensureTrayIcon(): TrayIcon? {
        if (trayIcon != null) return trayIcon
        if (GraphicsEnvironment.isHeadless() || !SystemTray.isSupported()) return null
        return try {
            val tray = SystemTray.getSystemTray()
            val image = createFallbackImage()
            val icon = TrayIcon(image, "Orchestra Dashboard")
            icon.isImageAutoSize = true
            icon.addActionListener {
                lastPayload?.let { payload -> actionListener?.invoke(payload) }
            }
            tray.add(icon)
            trayIcon = icon
            icon
        } catch (
            @Suppress("TooGenericExceptionCaught")
            t: Throwable,
        ) {
            null
        }
    }

    private fun createFallbackImage(): Image {
        // Try to load a platform default icon; if unavailable, return a 16x16 transparent buffer.
        val defaultImage =
            runCatching {
                Toolkit.getDefaultToolkit().getImage("")
            }.getOrNull()
        return defaultImage ?: BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
    }
}
