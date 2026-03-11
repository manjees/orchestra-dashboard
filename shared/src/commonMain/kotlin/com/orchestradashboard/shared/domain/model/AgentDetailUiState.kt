package com.orchestradashboard.shared.domain.model

data class AgentDetailUiState(
    val agent: Agent? = null,
    val pipelineRuns: List<PipelineRun> = emptyList(),
    val events: List<AgentEvent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTab: DetailTab = DetailTab.OVERVIEW,
)

enum class DetailTab {
    OVERVIEW,
    PIPELINES,
    EVENTS,
}
