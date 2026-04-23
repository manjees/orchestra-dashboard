package com.orchestradashboard.server.service.notification

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApnsSenderTest {
    @Test
    fun `NoopApnsSender send always returns true`() {
        val sender = NoopApnsSender()

        val result = sender.send("any-token", mapOf("pipelineId" to "pipe-1"))

        assertTrue(result)
    }
}
