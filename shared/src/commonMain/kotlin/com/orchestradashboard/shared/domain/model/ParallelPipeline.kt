package com.orchestradashboard.shared.domain.model

enum class DependencyType {
    BLOCKS_START,
    PROVIDES_INPUT,
}

data class PipelineDependency(
    val sourceLaneId: String,
    val targetLaneId: String,
    val type: DependencyType,
)

data class ParallelPipelineGroup(
    val parentPipelineId: String,
    val pipelines: List<MonitoredPipeline>,
    val dependencies: List<PipelineDependency>,
) {
    val overallStatus: PipelineRunStatus
        get() =
            when {
                pipelines.isEmpty() -> PipelineRunStatus.QUEUED
                pipelines.any { it.status == PipelineRunStatus.RUNNING } -> PipelineRunStatus.RUNNING
                pipelines.any { it.status == PipelineRunStatus.FAILED } -> PipelineRunStatus.FAILED
                pipelines.all { it.status == PipelineRunStatus.PASSED } -> PipelineRunStatus.PASSED
                else -> PipelineRunStatus.QUEUED
            }

    val progressFraction: Float
        get() {
            if (pipelines.isEmpty()) return 0f
            return pipelines.map { it.progressFraction }.average().toFloat()
        }

    val activeLaneCount: Int
        get() = pipelines.count { it.status == PipelineRunStatus.RUNNING }
}
