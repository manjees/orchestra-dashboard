package com.orchestradashboard.shared.ui.pipelinemonitor

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.domain.model.DependencyType
import com.orchestradashboard.shared.domain.model.MonitoredPipeline
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ParallelPipelineViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakePipelineMonitorRepository
    private lateinit var viewModel: PipelineMonitorViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePipelineMonitorRepository()
        viewModel = PipelineMonitorViewModel("p1", repository)
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    // ─── State management ────────────────────────────────────────────────────

    @Test
    fun `loadPipeline with parallel mode sets isParallel true in state`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            viewModel.loadPipeline()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isParallel)
        }

    @Test
    fun `loadParallelPipelines populates parallelPipelines in state`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.parallelGroup)
            assertEquals(2, state.parallelGroup!!.pipelines.size)
            assertEquals(2, state.parallelPipelines.size)
        }

    @Test
    fun `loadParallelPipelines sets error on failure`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.failure(RuntimeException("parallel load failed"))
            viewModel.loadPipeline()
            advanceUntilIdle()

            assertEquals("parallel load failed", viewModel.uiState.value.error)
        }

    @Test
    fun `isParallelView returns true when parallelPipelines is non-empty`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isParallelView)
        }

    @Test
    fun `isParallelView returns false when parallelPipelines is empty`() =
        runTest {
            // Default pipeline is sequential, so loadParallelPipelines is not called
            viewModel.loadPipeline()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isParallelView)
        }

    // ─── Event handling for parallel lanes ───────────────────────────────────

    @Test
    fun `step event with laneId updates correct parallel pipeline lane`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "step.started",
                    pipelineId = "p1",
                    laneId = "lane-1",
                    step = "testing",
                    elapsedSec = 0.0,
                ),
            )
            advanceUntilIdle()

            val lane1 = viewModel.uiState.value.parallelPipelines.first { it.id == "lane-1" }
            val step = lane1.steps.first { it.name == "testing" }
            assertEquals(StepStatus.RUNNING, step.status)
        }

    @Test
    fun `step event with laneId for nonexistent lane is ignored`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            viewModel.startObserving()
            advanceUntilIdle()

            val countBefore = viewModel.uiState.value.parallelPipelines.size

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "step.started",
                    pipelineId = "p1",
                    laneId = "lane-nonexistent",
                    step = "coding",
                ),
            )
            advanceUntilIdle()

            // State is unchanged
            assertEquals(countBefore, viewModel.uiState.value.parallelPipelines.size)
        }

    @Test
    fun `pipeline completed event with laneId updates lane status to PASSED`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "pipeline.completed", pipelineId = "p1", laneId = "lane-1"),
            )
            advanceUntilIdle()

            val lane1 = viewModel.uiState.value.parallelPipelines.first { it.id == "lane-1" }
            assertEquals(PipelineRunStatus.PASSED, lane1.status)
        }

    @Test
    fun `pipeline failed event with laneId updates lane status to FAILED`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "pipeline.failed", pipelineId = "p1", laneId = "lane-2"),
            )
            advanceUntilIdle()

            val lane2 = viewModel.uiState.value.parallelPipelines.first { it.id == "lane-2" }
            assertEquals(PipelineRunStatus.FAILED, lane2.status)
        }

    @Test
    fun `all parallel lanes completed sets group overallStatus PASSED`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "pipeline.completed", pipelineId = "p1", laneId = "lane-1"),
            )
            repository.eventsFlow.emit(
                PipelineEventDto(type = "pipeline.completed", pipelineId = "p1", laneId = "lane-2"),
            )
            advanceUntilIdle()

            val group = viewModel.uiState.value.parallelGroup
            assertNotNull(group)
            assertEquals(PipelineRunStatus.PASSED, group.overallStatus)
        }

    // ─── Dependency data ─────────────────────────────────────────────────────

    @Test
    fun `loadParallelPipelines includes dependency data`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.dependencies.size)
        }

    @Test
    fun `dependencies are accessible from state after load`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            advanceUntilIdle()

            val dep = viewModel.uiState.value.dependencies.first()
            assertEquals("lane-1", dep.sourceLaneId)
            assertEquals("lane-2", dep.targetLaneId)
            assertEquals(DependencyType.BLOCKS_START, dep.type)
        }

    // ─── Refresh ─────────────────────────────────────────────────────────────

    @Test
    fun `refresh reloads both main pipeline and parallel pipelines when isParallel`() =
        runTest {
            repository.pipelineDetailResult = Result.success(parallelPipeline())
            repository.parallelPipelinesResult = Result.success(FakePipelineMonitorRepository.defaultParallelGroup())
            viewModel.loadPipeline()
            advanceUntilIdle()

            val callsBefore = repository.getParallelPipelinesCallCount

            viewModel.refresh()
            advanceUntilIdle()

            assertTrue(repository.getParallelPipelinesCallCount > callsBefore)
        }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun parallelPipeline() =
        MonitoredPipeline(
            id = "p1",
            projectName = "test-project",
            issueNum = 1,
            issueTitle = "Test Issue",
            mode = "parallel",
            status = PipelineRunStatus.RUNNING,
            steps = emptyList(),
            startedAtMs = 1718447400000L,
            elapsedTotalSec = 0.0,
        )
}
