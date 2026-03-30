package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.MetricEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetricJpaRepository : JpaRepository<MetricEntity, String> {
    fun findByAgentIdAndNameAndTimestampBetween(
        agentId: String,
        name: String,
        start: Long,
        end: Long,
    ): List<MetricEntity>

    fun findByAgentIdAndTimestampBetween(
        agentId: String,
        start: Long,
        end: Long,
    ): List<MetricEntity>
}
