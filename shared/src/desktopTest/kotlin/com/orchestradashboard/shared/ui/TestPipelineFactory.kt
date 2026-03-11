package com.orchestradashboard.shared.ui

import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.PipelineStep
import com.orchestradashboard.shared.domain.model.StepStatus

object TestPipelineFactory {
    fun createRun(
        id: String = "run-1",
        agentId: String = "agent-1",
        pipelineName: String = "Build Pipeline",
        status: PipelineRunStatus = PipelineRunStatus.PASSED,
        steps: List<PipelineStep> =
            listOf(
                PipelineStep("Compile", StepStatus.PASSED, "", 5000L),
                PipelineStep("Test", StepStatus.PASSED, "", 10000L),
            ),
        startedAt: Long = 1000L,
        finishedAt: Long? = 16000L,
        triggerInfo: String = "manual",
    ): PipelineRun = PipelineRun(id, agentId, pipelineName, status, steps, startedAt, finishedAt, triggerInfo)

    fun createRunningRun(
        id: String = "run-2",
        agentId: String = "agent-1",
        pipelineName: String = "Deploy Pipeline",
    ): PipelineRun =
        PipelineRun(
            id = id,
            agentId = agentId,
            pipelineName = pipelineName,
            status = PipelineRunStatus.RUNNING,
            steps =
                listOf(
                    PipelineStep("Build", StepStatus.PASSED, "", 3000L),
                    PipelineStep("Deploy", StepStatus.RUNNING, "", 0L),
                ),
            startedAt = 1000L,
            finishedAt = null,
            triggerInfo = "auto",
        )
}
