package com.orchestradashboard.desktop.notification

import com.orchestradashboard.shared.domain.model.NotificationStatus
import com.orchestradashboard.shared.domain.repository.NotificationRepository
import com.orchestradashboard.shared.push.DesktopPushNotificationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Wires the shared [DesktopPushNotificationProvider] into the desktop app's
 * SystemTray, relays tray-click events as deep-links, and ensures that each
 * incoming pipeline payload is shown as a system notification respecting the
 * user's notification settings.
 *
 * Incoming payloads are emitted via [DesktopPushNotificationProvider.emitIncoming]
 * — future server-push wiring (WebSocket consumer) can call that entry point.
 */
class DesktopNotificationService(
    private val notificationRepository: NotificationRepository,
    private val pushProvider: DesktopPushNotificationProvider,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    private val _deepLinkPipelineIds = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val deepLinkPipelineIds: SharedFlow<String> = _deepLinkPipelineIds.asSharedFlow()

    fun start() {
        pushProvider.setActionListener { payload ->
            _deepLinkPipelineIds.tryEmit(payload.pipelineId)
        }

        job?.cancel()
        job =
            scope.launch {
                notificationRepository.observeIncomingNotifications().collect { payload ->
                    val settings = notificationRepository.observeNotificationSettings().first()
                    if (!settings.enabled) return@collect
                    val allowed =
                        when (payload.status) {
                            NotificationStatus.SUCCESS -> settings.notifyOnSuccess
                            NotificationStatus.FAILURE -> settings.notifyOnFailure
                        }
                    if (!allowed) return@collect
                    pushProvider.showLocalNotification(payload)
                }
            }
    }

    fun stop() {
        job?.cancel()
        job = null
        scope.cancel()
    }
}
