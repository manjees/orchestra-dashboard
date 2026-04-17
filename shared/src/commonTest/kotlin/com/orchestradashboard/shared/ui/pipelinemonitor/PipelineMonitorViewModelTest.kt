package com.orchestradashboard.shared.ui.pipelinemonitor

import com.orchestradashboard.shared.data.dto.orchestrator.ApprovalContextDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.domain.model.ConnectionStatus
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.domain.usecase.RespondToApprovalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PipelineMonitorViewModelTest {
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

    @Test
    fun `initial state has isLoading false and null pipeline`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.pipeline)
        assertNull(state.error)
        assertEquals(ConnectionStatus.DISCONNECTED, state.connectionStatus)
    }

    @Test
    fun `loadPipeline sets isLoading true then false`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `loadPipeline sets pipeline on success`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.pipeline)
            assertEquals("p1", state.pipeline!!.id)
            assertEquals("test-project", state.pipeline!!.projectName)
            assertEquals(3, state.pipeline!!.steps.size)
        }

    @Test
    fun `loadPipeline sets error on failure`() =
        runTest {
            repository.pipelineDetailResult = Result.failure(RuntimeException("API error"))
            viewModel.loadPipeline()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.pipeline)
            assertEquals("API error", state.error)
            assertFalse(state.isLoading)
        }

    @Test
    fun `startObserving sets connectionStatus to CONNECTED`() =
        runTest {
            viewModel.startObserving()
            advanceUntilIdle()
            assertEquals(ConnectionStatus.CONNECTED, viewModel.uiState.value.connectionStatus)
        }

    @Test
    fun `step started event updates step status to RUNNING`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "step.started", pipelineId = "p1", step = "testing", elapsedSec = 0.0),
            )
            advanceUntilIdle()

            val step = viewModel.uiState.value.pipeline!!.steps.first { it.name == "testing" }
            assertEquals(StepStatus.RUNNING, step.status)
            assertNotNull(step.startedAtMs)
        }

    @Test
    fun `step completed event updates step status to PASSED and sets elapsedMs`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "step.completed", pipelineId = "p1", step = "coding", elapsedSec = 15.0),
            )
            advanceUntilIdle()

            val step = viewModel.uiState.value.pipeline!!.steps.first { it.name == "coding" }
            assertEquals(StepStatus.PASSED, step.status)
            assertEquals(15000L, step.elapsedMs)
            assertNull(step.startedAtMs)
        }

    @Test
    fun `step failed event updates step status to FAILED`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "step.failed", pipelineId = "p1", step = "testing", elapsedSec = 3.0),
            )
            advanceUntilIdle()

            val step = viewModel.uiState.value.pipeline!!.steps.first { it.name == "testing" }
            assertEquals(StepStatus.FAILED, step.status)
        }

    @Test
    fun `pipeline completed event updates pipeline status to PASSED`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "pipeline.completed", pipelineId = "p1"),
            )
            advanceUntilIdle()

            assertEquals(PipelineRunStatus.PASSED, viewModel.uiState.value.pipeline!!.status)
        }

    @Test
    fun `pipeline failed event updates pipeline status to FAILED`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "pipeline.failed", pipelineId = "p1"),
            )
            advanceUntilIdle()

            assertEquals(PipelineRunStatus.FAILED, viewModel.uiState.value.pipeline!!.status)
        }

    @Test
    fun `approval requested event sets pendingApproval in state`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "approval.requested",
                    pipelineId = "p1",
                    approvalId = "approval-1",
                    approvalType = "strategy",
                    options = listOf("approve", "reject"),
                    timeoutSec = 60,
                ),
            )
            advanceUntilIdle()

            val approval = viewModel.uiState.value.pendingApproval
            assertNotNull(approval)
            assertEquals("strategy", approval.approvalType)
            assertEquals(listOf("approve", "reject"), approval.options)
            assertEquals("approval-1", approval.id)
            assertEquals(60, approval.timeoutSec)
        }

    @Test
    fun `supreme court required event sets pendingApproval in state`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "supreme_court.required",
                    pipelineId = "p1",
                    approvalId = "sc-1",
                    approvalType = "supreme_court",
                    options = listOf("uphold", "overturn", "redesign"),
                    context = ApprovalContextDto(eta = "5m", detail = "Test ruling"),
                    timeoutSec = 120,
                ),
            )
            advanceUntilIdle()

            val approval = viewModel.uiState.value.pendingApproval
            assertNotNull(approval)
            assertEquals("supreme_court", approval.approvalType)
            assertEquals("sc-1", approval.id)
            assertNotNull(approval.context)
            assertEquals("5m", approval.context?.eta)
            assertEquals("Test ruling", approval.context?.detail)
        }

    @Test
    fun `approval event starts countdown and remainingTimeSec decreases`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "approval.requested",
                    pipelineId = "p1",
                    approvalId = "a1",
                    approvalType = "strategy",
                    options = listOf("approve"),
                    timeoutSec = 10,
                ),
            )
            runCurrent()

            assertEquals(10, viewModel.uiState.value.remainingTimeSec)

            advanceTimeBy(3001)
            runCurrent()

            assertEquals(7, viewModel.uiState.value.remainingTimeSec)
            assertFalse(viewModel.uiState.value.isApprovalTimedOut)
        }

    @Test
    fun `countdown reaching zero sets isApprovalTimedOut`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "approval.requested",
                    pipelineId = "p1",
                    approvalId = "a1",
                    approvalType = "strategy",
                    options = listOf("approve"),
                    timeoutSec = 3,
                ),
            )
            advanceUntilIdle()

            advanceTimeBy(4000)

            assertEquals(0, viewModel.uiState.value.remainingTimeSec)
            assertTrue(viewModel.uiState.value.isApprovalTimedOut)
        }

    @Test
    fun `respondToApproval calls use case and clears approval`() =
        runTest {
            val fakeApprovalRepo = FakeApprovalRepository()
            val useCase = RespondToApprovalUseCase(fakeApprovalRepo)
            val vm = PipelineMonitorViewModel("p1", repository, useCase)

            vm.loadPipeline()
            advanceUntilIdle()

            vm.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "approval.requested",
                    pipelineId = "p1",
                    approvalId = "a1",
                    approvalType = "strategy",
                    options = listOf("approve"),
                    timeoutSec = 60,
                ),
            )
            advanceUntilIdle()

            assertNotNull(vm.uiState.value.pendingApproval)

            vm.respondToApproval("split_execute")
            advanceUntilIdle()

            assertEquals(1, fakeApprovalRepo.respondCallCount)
            assertEquals("a1", fakeApprovalRepo.lastApprovalId)
            assertEquals("split_execute", fakeApprovalRepo.lastDecision)
            assertNull(vm.uiState.value.pendingApproval)
            assertNull(vm.uiState.value.remainingTimeSec)

            vm.onCleared()
        }

    @Test
    fun `respondToApproval failure sets error`() =
        runTest {
            val fakeApprovalRepo = FakeApprovalRepository()
            fakeApprovalRepo.respondResult = Result.failure(RuntimeException("Network error"))
            val useCase = RespondToApprovalUseCase(fakeApprovalRepo)
            val vm = PipelineMonitorViewModel("p1", repository, useCase)

            vm.loadPipeline()
            advanceUntilIdle()

            vm.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "approval.requested",
                    pipelineId = "p1",
                    approvalId = "a1",
                    approvalType = "strategy",
                    options = listOf("approve"),
                ),
            )
            advanceUntilIdle()

            vm.respondToApproval("approve")
            advanceUntilIdle()

            assertEquals("Network error", vm.uiState.value.error)
            // Approval should remain since the call failed
            assertNotNull(vm.uiState.value.pendingApproval)

            vm.onCleared()
        }

    @Test
    fun `log event appends to logLines`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "log", pipelineId = "p1", detail = "Building module..."),
            )
            repository.eventsFlow.emit(
                PipelineEventDto(type = "log", pipelineId = "p1", detail = "Tests running..."),
            )
            advanceUntilIdle()

            assertEquals(2, viewModel.uiState.value.logLines.size)
            assertEquals("Building module...", viewModel.uiState.value.logLines[0])
            assertEquals("Tests running...", viewModel.uiState.value.logLines[1])
        }

    @Test
    fun `clearError sets error to null`() =
        runTest {
            repository.pipelineDetailResult = Result.failure(RuntimeException("error"))
            viewModel.loadPipeline()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    @Test
    fun `refresh reloads pipeline data`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()
            assertEquals(1, repository.getPipelineDetailCallCount)

            viewModel.refresh()
            advanceUntilIdle()
            assertEquals(2, repository.getPipelineDetailCallCount)
        }

    @Test
    fun `dismissApproval clears pendingApproval and remainingTimeSec`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(
                    type = "approval.requested",
                    pipelineId = "p1",
                    approvalType = "supreme_court",
                    options = listOf("approve"),
                    timeoutSec = 60,
                ),
            )
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.pendingApproval)
            assertNotNull(viewModel.uiState.value.remainingTimeSec)

            viewModel.dismissApproval()

            assertNull(viewModel.uiState.value.pendingApproval)
            assertNull(viewModel.uiState.value.remainingTimeSec)
        }

    @Test
    fun `step event with unknown step name does not crash`() =
        runTest {
            viewModel.loadPipeline()
            advanceUntilIdle()

            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "step.started", pipelineId = "p1", step = "nonexistent"),
            )
            advanceUntilIdle()

            assertEquals(3, viewModel.uiState.value.pipeline!!.steps.size)
        }

    @Test
    fun `step event before pipeline loaded is ignored`() =
        runTest {
            viewModel.startObserving()
            advanceUntilIdle()

            repository.eventsFlow.emit(
                PipelineEventDto(type = "step.started", pipelineId = "p1", step = "coding"),
            )
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.pipeline)
        }
}
