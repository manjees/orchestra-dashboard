package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.TimeSeriesDataResponse
import com.orchestradashboard.server.service.MetricsAggregationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/metrics")
class MetricsController(
    private val metricsAggregationService: MetricsAggregationService,
) {
    @GetMapping("/{agentId}/aggregate")
    fun getAggregatedMetrics(
        @PathVariable agentId: String,
        @RequestParam(required = false) from: Long?,
        @RequestParam(required = false) to: Long?,
        @RequestParam(required = false) metricName: String?,
    ): ResponseEntity<List<TimeSeriesDataResponse>> {
        val results = metricsAggregationService.getAggregatedMetrics(agentId, from, to, metricName)
        return ResponseEntity.ok(results)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(mapOf("error" to (ex.message ?: "Not found")))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(mapOf("error" to (ex.message ?: "Bad request")))
}
