package com.orchestradashboard.server.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class MetricsAggregationScheduler(
    private val metricsAggregationService: MetricsAggregationService,
) {
    @Scheduled(fixedRate = 300_000)
    fun runAggregation() {
        metricsAggregationService.runScheduledAggregation()
    }
}
