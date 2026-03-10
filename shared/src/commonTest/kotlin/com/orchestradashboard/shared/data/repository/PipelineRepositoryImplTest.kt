package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.dto.PipelineStepDto
import com.orchestradashboard.shared.data.mapper.PipelineRunMapper
import com.orchestradashboard.shared.data.network.FakeDashboardApiClient
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PipelineRepositoryImplTest {
    private val fakeApi = FakeDashboardApiClient()
    private val mapper = PipelineRunMapper()
    private val repository = PipelineRepositoryImpl(fakeApi, mapper, pollingIntervalMs = 50L)

    private val sampleRunDto =
        PipelineRunDto(
            id = "run-1",
            agentId = "agent-1",
            pipelineName = "Build",
            status = "RUNNING",
            steps = listOf(PipelineStepDto("Compile", "PASSED", "OK", 1000L)),
            startedAt = 1700000000L,
        )

    @Test
    fun `getPipelineRun returns Result success with mapped domain model`() =
        runTest {
            fakeApi.pipelineRunResponse = sampleRunDto

            val result = repository.getPipelineRun("run-1")

            assertTrue(result.isSuccess)
            assertEquals("run-1", result.getOrThrow().id)
            assertEquals(PipelineRunStatus.RUNNING, result.getOrThrow().status)
            assertEquals(1, result.getOrThrow().steps.size)
        }

    @Test
    fun `getPipelineRun returns Result failure on network error`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("Network error")

            val result = repository.getPipelineRun("run-1")

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }

    @Test
    fun `observePipelineRuns emits pipeline list for given agentId`() =
        runTest {
            fakeApi.pipelineRunsResponse =
                listOf(
                    sampleRunDto,
                    PipelineRunDto("run-2", "agent-2", "Deploy", "PASSED", emptyList(), 100L, 200L),
                )

            val runs = repository.observePipelineRuns("agent-1").first()

            assertEquals(1, runs.size)
            assertEquals("run-1", runs[0].id)
        }

    @Test
    fun `observeActivePipelines emits only active pipelines`() =
        runTest {
            fakeApi.pipelineRunsResponse =
                listOf(
                    // RUNNING
                    sampleRunDto,
                    PipelineRunDto("run-2", "agent-2", "Deploy", "PASSED", emptyList(), 100L, 200L),
                    PipelineRunDto("run-3", "agent-3", "Test", "QUEUED", emptyList(), 300L),
                )

            val active = repository.observeActivePipelines().first()

            assertEquals(2, active.size)
            assertTrue(active.all { it.status == PipelineRunStatus.RUNNING || it.status == PipelineRunStatus.QUEUED })
        }

    @Test
    fun `observePipelineRuns emits periodically`() =
        runTest {
            fakeApi.pipelineRunsResponse = listOf(sampleRunDto)

            val emissions = repository.observePipelineRuns("agent-1").take(3).toList()

            assertEquals(3, emissions.size)
            assertTrue(fakeApi.getPipelineRunsCallCount >= 3)
        }
}
