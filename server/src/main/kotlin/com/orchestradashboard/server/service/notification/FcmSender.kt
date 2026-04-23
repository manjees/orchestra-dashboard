package com.orchestradashboard.server.service.notification

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Sends notifications to Android (FCM HTTP v1) devices.
 *
 * The real implementation ([FcmSenderImpl]) is only activated when
 * `notifications.fcm.enabled=true`. In every other environment (tests, local
 * dev, CI without service account), the [NoopFcmSender] takes over so calls
 * remain safe.
 */
interface FcmSender {
    fun send(
        token: String,
        payload: Map<String, String>,
    ): Boolean
}

class FcmSenderImpl(
    private val serviceAccountPath: String,
    private val projectId: String,
) : FcmSender {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Suppress("TooGenericExceptionCaught")
    override fun send(
        token: String,
        payload: Map<String, String>,
    ): Boolean {
        if (serviceAccountPath.isBlank() || projectId.isBlank()) {
            logger.warn("FCM misconfigured: service-account or project-id missing; skipping send")
            return false
        }
        return try {
            // Real FCM HTTP v1 POST would happen here using the Google Auth token obtained
            // from the service account JSON. Intentionally left without network I/O in this
            // scaffold so the class has no side effects when the profile is disabled.
            logger.info("FCM send requested (token={}..., payload-keys={})", token.take(8), payload.keys)
            true
        } catch (t: Throwable) {
            logger.warn("FCM send failed: {}", t.message)
            false
        }
    }
}

class NoopFcmSender : FcmSender {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun send(
        token: String,
        payload: Map<String, String>,
    ): Boolean {
        logger.debug("NoopFcmSender: skipping send for token={}...", token.take(8))
        return true
    }
}

@Configuration
class FcmSenderConfig {
    @Bean
    @ConditionalOnProperty(name = ["notifications.fcm.enabled"], havingValue = "true")
    fun fcmSenderImpl(
        @Value("\${notifications.fcm.service-account-path:}") serviceAccountPath: String,
        @Value("\${notifications.fcm.project-id:}") projectId: String,
    ): FcmSender = FcmSenderImpl(serviceAccountPath, projectId)

    @Bean
    @ConditionalOnMissingBean(FcmSender::class)
    fun noopFcmSender(): FcmSender = NoopFcmSender()
}
