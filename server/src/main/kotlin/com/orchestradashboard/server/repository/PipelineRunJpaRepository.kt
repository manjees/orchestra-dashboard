package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.PipelineRunEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PipelineRunJpaRepository : JpaRepository<PipelineRunEntity, String> {
    fun findByAgentId(
        agentId: String,
        pageable: Pageable,
    ): Page<PipelineRunEntity>

    fun findByStatus(
        status: String,
        pageable: Pageable,
    ): Page<PipelineRunEntity>

    fun findByAgentIdAndStatus(
        agentId: String,
        status: String,
        pageable: Pageable,
    ): Page<PipelineRunEntity>
}
