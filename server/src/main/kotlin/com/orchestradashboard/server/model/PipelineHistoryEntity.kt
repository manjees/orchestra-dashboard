package com.orchestradashboard.server.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.util.UUID

@Entity
@Table(name = "pipeline_history")
data class PipelineHistoryEntity(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "project_name", nullable = false)
    val projectName: String = "",
    @Column(name = "issue_num", nullable = false)
    val issueNum: Int = 0,
    @Column(name = "issue_title", nullable = false)
    val issueTitle: String = "",
    @Column(nullable = false, length = 20)
    val mode: String = "",
    @Column(nullable = false, length = 20)
    val status: String = "RUNNING",
    @Column(name = "started_at", nullable = false)
    val startedAt: Long = 0L,
    @Column(name = "completed_at")
    val completedAt: Long? = null,
    @Column(name = "elapsed_total_sec")
    val elapsedTotalSec: Double = 0.0,
    @Column(name = "pr_url")
    val prUrl: String? = null,
    @OneToMany(
        mappedBy = "pipelineHistory",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    val steps: List<PipelineStepHistoryEntity> = emptyList(),
    @Version
    val version: Long = 0L,
)
