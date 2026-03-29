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
@Table(name = "agent_commands")
data class AgentCommandEntity(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "agent_id", nullable = false, length = 36)
    val agentId: String = "",
    @Column(name = "command_type", nullable = false)
    val commandType: String = "",
    @Column(nullable = false)
    val status: String = "PENDING",
    @Column(name = "requested_at", nullable = false)
    val requestedAt: Long = 0L,
    @Column(name = "requested_by", nullable = false)
    val requestedBy: String = "",
    @Column(name = "executed_at")
    val executedAt: Long? = null,
    @Column(name = "completed_at")
    val completedAt: Long? = null,
    @Column(name = "failure_reason")
    val failureReason: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", insertable = false, updatable = false)
    val agent: AgentEntity? = null,
)
