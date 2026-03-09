package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveActivePipelinesUseCaseTest {
    private val runningPipeline =
        PipelineRun(
            id = "run-1",
            agentId = "agent-1",
            pipelineName = "Build",
            status = PipelineRunStatus.RUNNING,
            steps = emptyList(),
            startedAt = 1000L,
            finishedAt = null,
            triggerInfo = "manual",
        )
    private val queuedPipeline =
        PipelineRun(
            id = "run-2",
            agentId = "agent-2",
            pipelineName = "Deploy",
            status = PipelineRunStatus.QUEUED,
            steps = emptyList(),
            startedAt = 2000L,
            finishedAt = null,
            triggerInfo = "auto",
        )
    private val passedPipeline =
        PipelineRun(
            id = "run-3",
            agentId = "agent-1",
            pipelineName = "Test",
            status = PipelineRunStatus.PASSED,
            steps = emptyList(),
            startedAt = 500L,
            finishedAt = 900L,
            triggerInfo = "commit",
        )

    @Test
    fun `invoke returns only active pipelines`() =
        runTest {
            val repository =
                FakePipelineRepository(listOf(runningPipeline, queuedPipeline, passedPipeline))
            val useCase = ObserveActivePipelinesUseCase(repository)

            val result = useCase().first()

            assertEquals(2, result.size)
            assertTrue(result.contains(runningPipeline))
            assertTrue(result.contains(queuedPipeline))
        }

    @Test
    fun `invoke returns empty list when no active pipelines`() =
        runTest {
            val repository = FakePipelineRepository(listOf(passedPipeline))
            val useCase = ObserveActivePipelinesUseCase(repository)

            val result = useCase().first()

            assertTrue(result.isEmpty())
        }
}
