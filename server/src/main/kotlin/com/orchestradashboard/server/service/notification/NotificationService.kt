package com.orchestradashboard.server.service.notification

import com.orchestradashboard.server.model.notification.DeviceTokenRecord
import com.orchestradashboard.server.model.notification.DeviceTokenRequest
import com.orchestradashboard.server.model.notification.DeviceTokenResponse
import com.orchestradashboard.server.model.notification.NotificationDispatchResult
import com.orchestradashboard.server.model.notification.PipelineNotificationPayload
import com.orchestradashboard.server.repository.notification.DeviceTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val tokenRepository: DeviceTokenRepository,
    private val fcmSender: FcmSender,
    private val apnsSender: ApnsSender,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun registerToken(request: DeviceTokenRequest): DeviceTokenResponse {
        val platform = request.platform.uppercase()
        val now = System.currentTimeMillis()
        val record = DeviceTokenRecord(token = request.token, platform = platform, createdAt = now)
        tokenRepository.save(record)
        logger.info("Registered device token={}... platform={}", request.token.take(8), platform)
        return DeviceTokenResponse(registeredAt = now)
    }

    fun unregisterToken(token: String): Boolean {
        val removed = tokenRepository.remove(token)
        if (removed) {
            logger.info("Unregistered device token={}...", token.take(8))
        }
        return removed
    }

    fun dispatch(payload: PipelineNotificationPayload): NotificationDispatchResult {
        val tokens = tokenRepository.findAll()
        if (tokens.isEmpty()) {
            logger.debug("No registered tokens to notify for pipeline={}", payload.pipelineId)
            return NotificationDispatchResult(attempted = 0, succeeded = 0, failed = 0)
        }
        val data = buildPayloadMap(payload)
        var succeeded = 0
        var failed = 0
        tokens.forEach { record ->
            val ok =
                when (record.platform.uppercase()) {
                    PLATFORM_ANDROID -> fcmSender.send(record.token, data)
                    PLATFORM_IOS -> apnsSender.send(record.token, data)
                    else -> {
                        logger.debug("Skipping unsupported platform={}", record.platform)
                        true
                    }
                }
            if (ok) succeeded++ else failed++
        }
        logger.info(
            "Dispatched notifications for pipeline={} attempted={} succeeded={} failed={}",
            payload.pipelineId,
            tokens.size,
            succeeded,
            failed,
        )
        return NotificationDispatchResult(attempted = tokens.size, succeeded = succeeded, failed = failed)
    }

    private fun buildPayloadMap(payload: PipelineNotificationPayload): Map<String, String> {
        val map =
            mutableMapOf(
                "pipelineId" to payload.pipelineId,
                "projectName" to payload.projectName,
                "status" to payload.status,
                "timestamp" to payload.timestamp.toString(),
            )
        payload.issueNumber?.let { map["issueNumber"] = it.toString() }
        payload.prUrl?.let { map["prUrl"] = it }
        return map.toMap()
    }

    private companion object {
        const val PLATFORM_ANDROID = "ANDROID"
        const val PLATFORM_IOS = "IOS"
    }
}
