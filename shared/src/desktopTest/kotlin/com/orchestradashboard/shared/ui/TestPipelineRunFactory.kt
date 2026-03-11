package com.orchestradashboard.shared.ui

import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.PipelineStep
import com.orchestradashboard.shared.domain.model.StepStatus
import kotlinx.datetime.Clock

object TestPipelineRunFactory {
    fun createStep(
        name: String = "Build",
        status: StepStatus = StepStatus.PASSED,
        detail: String = "",
        elapsedMs: Long = 1200L,
    ): PipelineStep =
        PipelineStep(
            name = name,
            status = status,
            detail = detail,
            elapsedMs = elapsedMs,
        )

    fun create(
        id: String = "run-1",
        agentId: String = "agent-1",
        pipelineName: String = "deploy-pipeline",
        status: PipelineRunStatus = PipelineRunStatus.PASSED,
        steps: List<PipelineStep> =
            listOf(
                createStep(name = "Build", status = StepStatus.PASSED),
                createStep(name = "Test", status = StepStatus.PASSED),
                createStep(name = "Deploy", status = StepStatus.PASSED),
            ),
        startedAt: Long = Clock.System.now().toEpochMilliseconds() - 60_000L,
        finishedAt: Long? = Clock.System.now().toEpochMilliseconds(),
        triggerInfo: String = "manual",
    ): PipelineRun =
        PipelineRun(
            id = id,
            agentId = agentId,
            pipelineName = pipelineName,
            status = status,
            steps = steps,
            startedAt = startedAt,
            finishedAt = finishedAt,
            triggerInfo = triggerInfo,
        )

    fun createList(): List<PipelineRun> =
        listOf(
            create(id = "run-1", pipelineName = "deploy-pipeline", status = PipelineRunStatus.PASSED),
            create(id = "run-2", pipelineName = "test-pipeline", status = PipelineRunStatus.FAILED),
            create(id = "run-3", pipelineName = "build-pipeline", status = PipelineRunStatus.RUNNING, finishedAt = null),
        )
}
