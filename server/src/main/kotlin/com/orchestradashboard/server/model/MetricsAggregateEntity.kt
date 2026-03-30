package com.orchestradashboard.server.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "metrics_aggregates")
data class MetricsAggregateEntity(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "agent_id", nullable = false, length = 36)
    val agentId: String = "",
    @Column(name = "metric_name", nullable = false, length = 100)
    val metricName: String = "",
    @Column(name = "avg_value", nullable = false)
    val avgValue: Double = 0.0,
    @Column(name = "min_value", nullable = false)
    val minValue: Double = 0.0,
    @Column(name = "max_value", nullable = false)
    val maxValue: Double = 0.0,
    @Column(name = "sample_count", nullable = false)
    val sampleCount: Int = 0,
    @Column(name = "window_start", nullable = false)
    val windowStart: Long = 0L,
    @Column(name = "window_end", nullable = false)
    val windowEnd: Long = 0L,
    @Column(name = "created_at", nullable = false)
    val createdAt: Long = 0L,
)
