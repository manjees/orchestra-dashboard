package com.orchestradashboard.shared.domain.model

data class MonitoredStep(
    val name: String,
    val status: StepStatus,
    val elapsedMs: Long,
    val startedAtMs: Long?,
    val detail: String,
)

data class ApprovalRequest(
    val approvalType: String,
    val options: List<String>,
)

data class MonitoredPipeline(
    val id: String,
    val projectName: String,
    val issueNum: Int,
    val issueTitle: String,
    val mode: String,
    val status: PipelineRunStatus,
    val steps: List<MonitoredStep>,
    val startedAtMs: Long?,
    val elapsedTotalSec: Double,
) {
    val currentRunningStep: MonitoredStep?
        get() = steps.firstOrNull { it.status == StepStatus.RUNNING }

    val progressFraction: Float
        get() {
            if (steps.isEmpty()) return 0f
            val completed =
                steps.count {
                    it.status == StepStatus.PASSED || it.status == StepStatus.FAILED || it.status == StepStatus.SKIPPED
                }
            return completed.toFloat() / steps.size
        }

    val isParallel: Boolean get() = mode == "parallel"
}
