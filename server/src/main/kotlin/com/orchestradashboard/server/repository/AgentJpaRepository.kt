package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AgentJpaRepository : JpaRepository<AgentEntity, String> {
    fun findByStatus(status: String): List<AgentEntity>

    fun findByStatus(
        status: String,
        pageable: Pageable,
    ): Page<AgentEntity>

    fun findByType(type: String): List<AgentEntity>
}
