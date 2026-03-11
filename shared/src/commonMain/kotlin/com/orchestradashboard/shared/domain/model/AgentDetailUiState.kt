package com.orchestradashboard.shared.domain.model

data class AgentDetailUiState(
    val agent: Agent? = null,
    val pipelineRuns: List<PipelineRun> = emptyList(),
    val events: List<AgentEvent> = emptyList(),
    val selectedTab: DetailTab = DetailTab.OVERVIEW,
    val isLoading: Boolean = false,
    val error: String? = null,
)

enum class DetailTab {
    OVERVIEW,
    PIPELINES,
    EVENTS,
}
