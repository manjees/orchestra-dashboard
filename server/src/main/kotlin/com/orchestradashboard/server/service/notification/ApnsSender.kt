package com.orchestradashboard.server.service.notification

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Sends notifications to iOS (APNs) devices.
 *
 * Real APNs integration requires an Apple Developer paid account
 * (Push Notifications capability + `.p8` auth key) and is tracked in a follow-up
 * issue. Until then, [NoopApnsSender] handles all dispatch calls as no-ops.
 */
interface ApnsSender {
    fun send(
        token: String,
        payload: Map<String, String>,
    ): Boolean
}

class NoopApnsSender : ApnsSender {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun send(
        token: String,
        payload: Map<String, String>,
    ): Boolean {
        logger.debug("NoopApnsSender: skipping send for token={}...", token.take(8))
        return true
    }
}

@Configuration
class ApnsSenderConfig {
    @Bean
    @ConditionalOnMissingBean(ApnsSender::class)
    fun noopApnsSender(): ApnsSender = NoopApnsSender()
}
