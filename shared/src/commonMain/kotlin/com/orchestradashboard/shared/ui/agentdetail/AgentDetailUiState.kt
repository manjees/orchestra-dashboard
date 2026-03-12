package com.orchestradashboard.shared.ui.agentdetail

import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.model.AgentEvent
import com.orchestradashboard.shared.domain.model.PipelineRun

data class AgentDetailUiState(
    val agent: Agent? = null,
    val pipelineRuns: List<PipelineRun> = emptyList(),
    val events: List<AgentEvent> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: DetailTab = DetailTab.OVERVIEW,
)
