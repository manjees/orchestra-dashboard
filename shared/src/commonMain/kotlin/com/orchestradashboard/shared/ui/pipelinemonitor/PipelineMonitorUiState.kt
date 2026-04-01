package com.orchestradashboard.shared.ui.pipelinemonitor

import com.orchestradashboard.shared.domain.model.ApprovalRequest
import com.orchestradashboard.shared.domain.model.ConnectionStatus
import com.orchestradashboard.shared.domain.model.MonitoredPipeline

data class PipelineMonitorUiState(
    val pipeline: MonitoredPipeline? = null,
    val logLines: List<String> = emptyList(),
    val pendingApproval: ApprovalRequest? = null,
    val parallelPipelines: List<MonitoredPipeline> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
) {
    val isParallelView: Boolean get() = parallelPipelines.isNotEmpty()

    val currentStepName: String? get() = pipeline?.currentRunningStep?.name
}
