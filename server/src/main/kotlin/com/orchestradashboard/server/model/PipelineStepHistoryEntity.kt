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
@Table(name = "pipeline_step_history")
data class PipelineStepHistoryEntity(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "pipeline_history_id", nullable = false, length = 36)
    val pipelineHistoryId: String = "",
    @Column(name = "step_name", nullable = false)
    val stepName: String = "",
    @Column(nullable = false, length = 20)
    val status: String = "PENDING",
    @Column(name = "elapsed_sec")
    val elapsedSec: Double = 0.0,
    @Column(name = "fail_detail", columnDefinition = "TEXT")
    val failDetail: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_history_id", insertable = false, updatable = false)
    val pipelineHistory: PipelineHistoryEntity? = null,
)
