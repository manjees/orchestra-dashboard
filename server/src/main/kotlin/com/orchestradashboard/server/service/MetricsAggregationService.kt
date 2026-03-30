package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.DataPointResponse
import com.orchestradashboard.server.model.MetricsAggregateEntity
import com.orchestradashboard.server.model.TimeSeriesDataResponse
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.MetricJpaRepository
import com.orchestradashboard.server.repository.MetricsAggregateJpaRepository
import org.springframework.stereotype.Service

@Service
class MetricsAggregationService(
    private val metricRepository: MetricJpaRepository,
    private val aggregateRepository: MetricsAggregateJpaRepository,
    private val agentRepository: AgentJpaRepository,
) {
    companion object {
        const val DEFAULT_WINDOW_MS = 3_600_000L
        const val AGGREGATION_INTERVAL_MS = 300_000L
    }

    fun getAggregatedMetrics(
        agentId: String,
        from: Long?,
        to: Long?,
        metricName: String?,
    ): List<TimeSeriesDataResponse> {
        agentRepository.findById(agentId)
            .orElseThrow { NoSuchElementException("Agent with id '$agentId' not found") }

        val endTs = to ?: System.currentTimeMillis()
        val startTs = from ?: (endTs - DEFAULT_WINDOW_MS)

        require(startTs <= endTs) { "'from' must be before 'to'" }

        if (metricName != null) {
            val metrics = metricRepository.findByAgentIdAndNameAndTimestampBetween(agentId, metricName, startTs, endTs)
            return listOf(buildTimeSeriesResponse(agentId, metricName, metrics.map { it.timestamp to it.value }, startTs, endTs))
        }

        val allMetrics = metricRepository.findByAgentIdAndTimestampBetween(agentId, startTs, endTs)
        return allMetrics
            .groupBy { it.name }
            .map { (name, entries) ->
                buildTimeSeriesResponse(agentId, name, entries.map { it.timestamp to it.value }, startTs, endTs)
            }
    }

    fun runScheduledAggregation() {
        val now = System.currentTimeMillis()
        val windowStart = now - AGGREGATION_INTERVAL_MS
        val agents = agentRepository.findAll()

        for (agent in agents) {
            val metrics = metricRepository.findByAgentIdAndTimestampBetween(agent.id, windowStart, now)
            metrics.groupBy { it.name }.forEach { (name, entries) ->
                val values = entries.map { it.value }
                if (values.isNotEmpty()) {
                    aggregateRepository.save(
                        MetricsAggregateEntity(
                            agentId = agent.id,
                            metricName = name,
                            avgValue = values.average(),
                            minValue = values.min(),
                            maxValue = values.max(),
                            sampleCount = values.size,
                            windowStart = windowStart,
                            windowEnd = now,
                            createdAt = now,
                        ),
                    )
                }
            }
        }
    }

    private fun buildTimeSeriesResponse(
        agentId: String,
        metricName: String,
        points: List<Pair<Long, Double>>,
        from: Long,
        to: Long,
    ): TimeSeriesDataResponse {
        val values = points.map { it.second }
        return TimeSeriesDataResponse(
            agentId = agentId,
            metricName = metricName,
            dataPoints = points.map { DataPointResponse(timestamp = it.first, value = it.second) },
            average = if (values.isNotEmpty()) Math.round(values.average() * 100.0) / 100.0 else null,
            min = values.minOrNull(),
            max = values.maxOrNull(),
            sampleCount = points.size,
            fromTimestamp = from,
            toTimestamp = to,
        )
    }
}
