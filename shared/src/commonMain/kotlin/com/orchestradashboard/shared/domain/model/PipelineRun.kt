package com.orchestradashboard.shared.domain.model

/**
 * Status of a pipeline run lifecycle.
 */
enum class PipelineRunStatus {
    QUEUED,
    RUNNING,
    PASSED,
    FAILED,
    CANCELLED,
}

/**
 * Status of an individual step within a pipeline.
 */
enum class StepStatus {
    PENDING,
    RUNNING,
    PASSED,
    FAILED,
    SKIPPED,
}

/**
 * Represents a single step within a pipeline run.
 *
 * @param name Human-readable step name
 * @param status Current execution status of this step
 * @param detail Additional information about the step execution
 * @param elapsedMs Time spent on this step in milliseconds
 */
data class PipelineStep(
    val name: String,
    val status: StepStatus,
    val detail: String,
    val elapsedMs: Long,
)

/**
 * Represents a single execution of an agent pipeline.
 *
 * @param id Unique identifier for this pipeline run
 * @param agentId The agent that owns this pipeline run
 * @param pipelineName Human-readable name of the pipeline
 * @param status Current status of the pipeline run
 * @param steps Ordered list of steps in this pipeline run
 * @param startedAt Unix epoch milliseconds when the run started
 * @param finishedAt Unix epoch milliseconds when the run finished, or null if still running
 * @param triggerInfo Description of what triggered this run
 */
data class PipelineRun(
    val id: String,
    val agentId: String,
    val pipelineName: String,
    val status: PipelineRunStatus,
    val steps: List<PipelineStep>,
    val startedAt: Long,
    val finishedAt: Long?,
    val triggerInfo: String,
) {
    /** Total duration in milliseconds, or null if the run has not finished */
    val duration: Long? get() = if (finishedAt != null) finishedAt - startedAt else null
}
