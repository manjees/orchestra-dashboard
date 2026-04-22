package com.orchestradashboard.shared.domain.model

data class HistoryDetail(
    val id: String,
    val projectName: String,
    val issueNum: Int,
    val issueTitle: String,
    val mode: String,
    val status: PipelineRunStatus,
    val startedAt: Long,
    val completedAt: Long?,
    val elapsedTotalSec: Double,
    val prUrl: String?,
    val steps: List<HistoryStep>,
)
