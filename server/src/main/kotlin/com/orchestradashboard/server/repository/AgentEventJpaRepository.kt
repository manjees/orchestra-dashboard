package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AgentEventJpaRepository : JpaRepository<AgentEventEntity, String> {
    fun findByAgentIdOrderByTimestampDesc(agentId: String): List<AgentEventEntity>

    fun findTop50ByOrderByTimestampDesc(): List<AgentEventEntity>
}
