package com.orchestradashboard.shared.ui.dashboardhome

import com.orchestradashboard.shared.domain.model.ActivePipeline
import com.orchestradashboard.shared.domain.model.ConnectionStatus
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.model.SystemStatus

data class DashboardHomeUiState(
    val systemStatus: SystemStatus? = null,
    val activePipelines: List<ActivePipeline> = emptyList(),
    val recentResults: List<PipelineResult> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
) {
    val hasActivePipelines: Boolean get() = activePipelines.isNotEmpty()
    val hasRecentResults: Boolean get() = recentResults.isNotEmpty()
}
