package com.orchestradashboard.server.service

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.scheduling.annotation.Scheduled

class MetricsAggregationSchedulerTest {
    private val metricsAggregationService: MetricsAggregationService = mock()
    private val scheduler = MetricsAggregationScheduler(metricsAggregationService)

    @Test
    fun `should schedule aggregation job on startup`() {
        val method = MetricsAggregationScheduler::class.java.getDeclaredMethod("runAggregation")
        val scheduledAnnotation = method.getAnnotation(Scheduled::class.java)

        assertNotNull(scheduledAnnotation)
        assertTrue(scheduledAnnotation.fixedRate == 300_000L)
    }

    @Test
    fun `should invoke service aggregation method when scheduled`() {
        scheduler.runAggregation()

        verify(metricsAggregationService).runScheduledAggregation()
    }
}
