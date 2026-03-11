package com.orchestradashboard.shared.domain.model

data class AgentDetailUiState(
    val agent: Agent? = null,
    val pipelineRuns: List<PipelineRun> = emptyList(),
    val events: List<AgentEvent> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTabIndex: Int = 0,
    val expandedPipelineIds: Set<String> = emptySet(),
)
