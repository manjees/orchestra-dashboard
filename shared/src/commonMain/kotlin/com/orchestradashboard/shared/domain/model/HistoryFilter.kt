package com.orchestradashboard.shared.domain.model

data class HistoryFilter(
    val project: String? = null,
    val status: PipelineRunStatus? = null,
    val keyword: String? = null,
    val timeRange: TimeRange? = null,
)
