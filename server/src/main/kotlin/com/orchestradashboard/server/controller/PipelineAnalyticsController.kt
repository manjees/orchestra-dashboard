package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.DurationTrendResponse
import com.orchestradashboard.server.model.PipelineAnalyticsResponse
import com.orchestradashboard.server.model.StepFailureRateResponse
import com.orchestradashboard.server.service.PipelineAnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/analytics/pipelines")
class PipelineAnalyticsController(
    private val analyticsService: PipelineAnalyticsService,
) {
    @GetMapping("/summary")
    fun getSummary(
        @RequestParam project: String,
        @RequestParam from: Long?,
        @RequestParam to: Long?,
    ): ResponseEntity<PipelineAnalyticsResponse> = ResponseEntity.ok(analyticsService.getSummary(project, from, to))

    @GetMapping("/step-failures")
    fun getStepFailures(
        @RequestParam project: String,
    ): ResponseEntity<List<StepFailureRateResponse>> = ResponseEntity.ok(analyticsService.getStepFailureRates(project))

    @GetMapping("/duration-trends")
    fun getDurationTrends(
        @RequestParam project: String,
        @RequestParam(defaultValue = "day") granularity: String,
    ): ResponseEntity<List<DurationTrendResponse>> = ResponseEntity.ok(analyticsService.getDurationTrends(project, granularity))
}
