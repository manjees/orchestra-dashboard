package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentCommandEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AgentCommandJpaRepository : JpaRepository<AgentCommandEntity, String> {
    fun findByAgentIdOrderByRequestedAtDesc(
        agentId: String,
        pageable: Pageable,
    ): List<AgentCommandEntity>

    fun findByAgentIdAndStatusIn(
        agentId: String,
        statuses: List<String>,
    ): List<AgentCommandEntity>

    fun findByStatusAndRequestedAtLessThan(
        status: String,
        cutoff: Long,
    ): List<AgentCommandEntity>

    fun findByStatusAndExecutedAtLessThan(
        status: String,
        cutoff: Long,
    ): List<AgentCommandEntity>
}
