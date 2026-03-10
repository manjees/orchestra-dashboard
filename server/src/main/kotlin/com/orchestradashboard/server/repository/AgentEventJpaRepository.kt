package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEventEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AgentEventJpaRepository : JpaRepository<AgentEventEntity, String> {
    fun findByAgentIdOrderByTimestampDesc(
        agentId: String,
        pageable: Pageable,
    ): List<AgentEventEntity>

    fun findAllByOrderByTimestampDesc(pageable: Pageable): List<AgentEventEntity>

    fun findTop50ByOrderByTimestampDesc(): List<AgentEventEntity>
}
