package com.orchestradashboard.shared.domain.model

/**
 * Represents a performance or operational metric emitted by an AI agent.
 *
 * @param agentId The ID of the agent that produced this metric
 * @param name Metric name (e.g., "tasks_completed", "latency_ms")
 * @param value Numeric metric value
 * @param unit Unit of measurement (e.g., "ms", "count", "percent")
 * @param timestamp Unix epoch milliseconds when the metric was recorded
 */
data class Metric(
    val agentId: String,
    val name: String,
    val value: Double,
    val unit: String,
    val timestamp: Long
)
