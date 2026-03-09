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
@Table(name = "pipeline_runs")
data class PipelineRunEntity(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "agent_id", nullable = false, length = 36)
    val agentId: String = "",
    @Column(name = "pipeline_name", nullable = false)
    val pipelineName: String = "",
    @Column(nullable = false)
    val status: String = "QUEUED",
    @Column(columnDefinition = "TEXT")
    val steps: String = "[]",
    @Column(name = "started_at", nullable = false)
    val startedAt: Long = 0L,
    @Column(name = "finished_at")
    val finishedAt: Long? = null,
    @Column(name = "trigger_info", columnDefinition = "TEXT")
    val triggerInfo: String = "",
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", insertable = false, updatable = false)
    val agent: AgentEntity? = null,
)
