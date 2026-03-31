package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.ui.pipelinemonitor.FakePipelineMonitorRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PipelineMonitorRepositoryImplTest {
    @Test
    fun `getPipelineDetail returns mapped MonitoredPipeline on success`() =
        runTest {
            val repo = FakePipelineMonitorRepository()
            val result = repo.getPipelineDetail("p1")
            assertTrue(result.isSuccess)
            assertEquals("p1", result.getOrNull()?.id)
        }

    @Test
    fun `getPipelineDetail returns failure when error configured`() =
        runTest {
            val repo = FakePipelineMonitorRepository()
            repo.pipelineDetailResult = Result.failure(RuntimeException("not found"))
            val result = repo.getPipelineDetail("p1")
            assertTrue(result.isFailure)
            assertEquals("not found", result.exceptionOrNull()?.message)
        }

    @Test
    fun `observePipelineEvents emits events with matching pipelineId`() =
        runTest {
            val repo = FakePipelineMonitorRepository()
            val collected = mutableListOf<PipelineEventDto>()

            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    repo.observePipelineEvents("p1").collect { collected.add(it) }
                }

            repo.eventsFlow.emit(PipelineEventDto(type = "step.started", pipelineId = "p1", step = "coding"))
            repo.eventsFlow.emit(PipelineEventDto(type = "step.started", pipelineId = "p2", step = "testing"))
            repo.eventsFlow.emit(PipelineEventDto(type = "log", pipelineId = "p1", detail = "hello"))

            job.cancel()

            assertEquals(2, collected.size)
            assertEquals("p1", collected[0].pipelineId)
            assertEquals("p1", collected[1].pipelineId)
        }

    @Test
    fun `observePipelineEvents skips events with null pipelineId`() =
        runTest {
            val repo = FakePipelineMonitorRepository()
            val collected = mutableListOf<PipelineEventDto>()

            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    repo.observePipelineEvents("p1").collect { collected.add(it) }
                }

            repo.eventsFlow.emit(PipelineEventDto(type = "system.status", pipelineId = null))
            repo.eventsFlow.emit(PipelineEventDto(type = "step.started", pipelineId = "p1", step = "a"))

            job.cancel()

            assertEquals(1, collected.size)
        }
}
