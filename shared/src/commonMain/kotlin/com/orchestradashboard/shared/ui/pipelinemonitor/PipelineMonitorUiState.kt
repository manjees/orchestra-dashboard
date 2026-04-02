package com.orchestradashboard.shared.ui.pipelinemonitor

import com.orchestradashboard.shared.domain.model.ApprovalRequest
import com.orchestradashboard.shared.domain.model.ConnectionStatus
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.ParallelPipelineGroup
import com.orchestradashboard.shared.domain.model.PipelineDependency

data class PipelineMonitorUiState(
    val pipeline: MonitoredPipeline? = null,
    val logLines: List<String> = emptyList(),
    val pendingApproval: ApprovalRequest? = null,
    val parallelPipelines: List<MonitoredPipeline> = emptyList(),
    val parallelGroup: ParallelPipelineGroup? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
) {
    val isParallel: Boolean get() = pipeline?.isParallel == true

    val isParallelView: Boolean get() = parallelGroup != null && parallelGroup.pipelines.isNotEmpty()

    val currentStepName: String? get() = pipeline?.currentRunningStep?.name

    val dependencies: List<PipelineDependency> get() = parallelGroup?.dependencies ?: emptyList()
}
