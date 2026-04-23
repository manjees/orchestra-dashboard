package com.orchestradashboard.android.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orchestradashboard.shared.domain.model.NotificationStatus
import com.orchestradashboard.shared.domain.model.PushNotificationPayload

class OrchestraMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushNotificationSetup.onNewFcmToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val pipelineId = data["pipelineId"] ?: return
        val statusRaw = data["status"] ?: "success"
        val status =
            when (statusRaw.lowercase()) {
                "failure", "failed" -> NotificationStatus.FAILURE
                else -> NotificationStatus.SUCCESS
            }
        val payload =
            PushNotificationPayload(
                projectName = data["projectName"].orEmpty(),
                pipelineId = pipelineId,
                status = status,
                timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
                issueNumber = data["issueNumber"]?.toIntOrNull(),
                prUrl = data["prUrl"],
            )
        PushNotificationSetup.onMessageReceived(payload)
    }
}
