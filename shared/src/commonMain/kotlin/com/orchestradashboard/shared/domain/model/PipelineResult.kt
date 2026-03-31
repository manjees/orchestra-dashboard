package com.orchestradashboard.shared.domain.model

data class PipelineResult(
    val id: String,
    val projectName: String,
    val issueNum: Int,
    val status: PipelineRunStatus,
    val elapsedTotalSec: Double,
    val completedAt: String?,
)
