package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AggregatedMetricEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AggregatedMetricJpaRepository : JpaRepository<AggregatedMetricEntity, String> {
    fun findByAgentIdAndTimestampBucketBetweenOrderByTimestampBucketAsc(
        agentId: String,
        startTime: Long,
        endTime: Long,
    ): List<AggregatedMetricEntity>

    fun findByAgentIdAndMetricNameAndTimestampBucketBetweenOrderByTimestampBucketAsc(
        agentId: String,
        metricName: String,
        startTime: Long,
        endTime: Long,
    ): List<AggregatedMetricEntity>
}
