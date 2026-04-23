package com.orchestradashboard.android.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.orchestradashboard.android.BuildConfig
import com.orchestradashboard.android.MainActivity
import com.orchestradashboard.shared.domain.model.DevicePlatform
import com.orchestradashboard.shared.domain.model.NotificationStatus
import com.orchestradashboard.shared.domain.model.PushNotificationPayload
import com.orchestradashboard.shared.domain.repository.NotificationRepository
import com.orchestradashboard.shared.push.AndroidPushNotificationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Wires the shared [AndroidPushNotificationProvider] into the Android FCM
 * infrastructure. Also provides utility helpers for the
 * [OrchestraMessagingService] to post local notifications / emit payloads.
 */
object PushNotificationSetup {
    private const val CHANNEL_ID = "pipeline_notifications"
    private const val CHANNEL_NAME = "Pipeline updates"
    private const val CHANNEL_DESCRIPTION = "Notifies when pipelines complete or fail."
    const val EXTRA_PIPELINE_ID = "pipelineId"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var provider: AndroidPushNotificationProvider? = null
    private var repository: NotificationRepository? = null
    private var appContext: Context? = null

    fun initialize(
        context: Context,
        notificationRepository: NotificationRepository,
        pushProvider: AndroidPushNotificationProvider,
    ) {
        appContext = context.applicationContext
        provider = pushProvider
        repository = notificationRepository
        ensureChannel(context)

        pushProvider.localNotificationHandler = { payload ->
            appContext?.let { showSystemNotification(it, payload) }
        }
        pushProvider.tokenFetcher = { fetchTokenSafely() }

        if (BuildConfig.FCM_ENABLED) {
            scope.launch {
                val token = fetchTokenSafely() ?: return@launch
                notificationRepository.registerDeviceToken(token, DevicePlatform.ANDROID)
            }
        }
    }

    /** Called by [OrchestraMessagingService] when FCM delivers a new token. */
    fun onNewFcmToken(token: String) {
        val repo = repository ?: return
        scope.launch {
            repo.registerDeviceToken(token, DevicePlatform.ANDROID)
        }
    }

    /** Called by [OrchestraMessagingService] on incoming push. */
    fun onMessageReceived(payload: PushNotificationPayload) {
        provider?.emitIncoming(payload)
        appContext?.let { showSystemNotification(it, payload) }
    }

    private suspend fun fetchTokenSafely(): String? {
        if (!BuildConfig.FCM_ENABLED) return null
        return runCatching { awaitFcmToken() }
            .getOrNull()
    }

    private suspend fun awaitFcmToken(): String? =
        suspendCancellableCoroutine { cont ->
            try {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(task.result)
                    } else {
                        cont.resumeWithException(task.exception ?: RuntimeException("FCM token fetch failed"))
                    }
                }
            } catch (
                @Suppress("TooGenericExceptionCaught", "SwallowedException")
                t: Throwable,
            ) {
                // Firebase not initialised (no google-services.json) — safe fallback.
                cont.resume(null)
            }
        }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = CHANNEL_DESCRIPTION
                }
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(channel)
        }
    }

    private fun showSystemNotification(
        context: Context,
        payload: PushNotificationPayload,
    ) {
        val title =
            when (payload.status) {
                NotificationStatus.SUCCESS -> "Pipeline Completed"
                NotificationStatus.FAILURE -> "Pipeline Failed"
            }
        val body =
            buildString {
                append(payload.projectName)
                payload.issueNumber?.let { append(" #").append(it) }
                append(" (").append(payload.pipelineId).append(")")
            }

        val intent =
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra(EXTRA_PIPELINE_ID, payload.pipelineId)
            }
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                payload.pipelineId.hashCode(),
                intent,
                pendingFlags,
            )

        val notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.notify(payload.pipelineId.hashCode(), notification)
    }
}
