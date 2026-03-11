package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.dto.PipelineStepDto
import com.orchestradashboard.shared.data.mapper.PipelineRunMapper
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PipelineRepositoryImplTest {
    private val mapper = PipelineRunMapper()

    private fun createFakeClient() = FakeDashboardApiClient(pollingIntervalMs = 5000L)

    private val sampleRunDto =
        PipelineRunDto(
            id = "run-1",
            agentId = "agent-1",
            pipelineName = "build-pipeline",
            status = "RUNNING",
            steps =
                listOf(
                    PipelineStepDto(
                        name = "compile",
                        status = "PASSED",
                        detail = "Compiled successfully",
                        elapsedMs = 1200L,
                    ),
                ),
            startedAt = 1000L,
            finishedAt = null,
            triggerInfo = "manual",
        )

    private val queuedRunDto =
        PipelineRunDto(
            id = "run-2",
            agentId = "agent-2",
            pipelineName = "test-pipeline",
            status = "QUEUED",
            steps = emptyList(),
            startedAt = 2000L,
            finishedAt = null,
            triggerInfo = "scheduled",
        )

    private val passedRunDto =
        PipelineRunDto(
            id = "run-3",
            agentId = "agent-1",
            pipelineName = "deploy-pipeline",
            status = "PASSED",
            steps = emptyList(),
            startedAt = 500L,
            finishedAt = 1500L,
            triggerInfo = "ci",
        )

    @Test
    fun `observePipelineRuns emits runs for agent`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.pipelineRuns = listOf(sampleRunDto, queuedRunDto, passedRunDto)
            val repo = PipelineRepositoryImpl(fakeClient, mapper)

            val result = repo.observePipelineRuns("agent-1").first()

            assertEquals(2, result.size)
            assertTrue(result.all { it.agentId == "agent-1" })
        }

    @Test
    fun `getPipelineRun returns success`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.pipelineRuns = listOf(sampleRunDto)
            val repo = PipelineRepositoryImpl(fakeClient, mapper)

            val result = repo.getPipelineRun("run-1")

            assertTrue(result.isSuccess)
            val run = result.getOrThrow()
            assertEquals("run-1", run.id)
            assertEquals("build-pipeline", run.pipelineName)
            assertEquals(PipelineRunStatus.RUNNING, run.status)
            assertEquals(1, run.steps.size)
        }

    @Test
    fun `getPipelineRun returns failure on network error`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.shouldFail = true
            val repo = PipelineRepositoryImpl(fakeClient, mapper)

            val result = repo.getPipelineRun("run-1")

            assertTrue(result.isFailure)
        }

    @Test
    fun `observeActivePipelines filters RUNNING and QUEUED`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.pipelineRuns = listOf(sampleRunDto, queuedRunDto, passedRunDto)
            val repo = PipelineRepositoryImpl(fakeClient, mapper)

            val result = repo.observeActivePipelines().first()

            assertEquals(2, result.size)
            assertTrue(
                result.all {
                    it.status == PipelineRunStatus.RUNNING || it.status == PipelineRunStatus.QUEUED
                },
            )
        }
}
