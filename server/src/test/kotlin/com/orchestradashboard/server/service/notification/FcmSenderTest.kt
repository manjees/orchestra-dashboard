package com.orchestradashboard.server.service.notification

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FcmSenderTest {
    @Test
    fun `NoopFcmSender send always returns true`() {
        val sender = NoopFcmSender()

        val result = sender.send("any-token", mapOf("pipelineId" to "pipe-1"))

        assertTrue(result)
    }
}
