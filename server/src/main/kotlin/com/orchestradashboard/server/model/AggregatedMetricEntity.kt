package com.orchestradashboard.server.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "aggregated_metrics")
data class AggregatedMetricEntity(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "agent_id", nullable = false, length = 36)
    val agentId: String = "",
    @Column(name = "metric_name", nullable = false)
    val metricName: String = "",
    @Column(name = "avg_value", nullable = false)
    val avgValue: Double = 0.0,
    @Column(name = "min_value", nullable = false)
    val minValue: Double = 0.0,
    @Column(name = "max_value", nullable = false)
    val maxValue: Double = 0.0,
    @Column(nullable = false)
    val count: Int = 0,
    @Column(name = "timestamp_bucket", nullable = false)
    val timestampBucket: Long = 0L,
    @Column(name = "created_at", nullable = false)
    val createdAt: Long = System.currentTimeMillis(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", insertable = false, updatable = false)
    val agent: AgentEntity? = null,
)
