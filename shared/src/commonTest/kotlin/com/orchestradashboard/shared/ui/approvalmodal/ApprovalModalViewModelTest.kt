package com.orchestradashboard.shared.ui.approvalmodal

import com.orchestradashboard.shared.data.dto.orchestrator.ApprovalContextDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.domain.model.GenericDecision
import com.orchestradashboard.shared.domain.model.StrategyDecision
import com.orchestradashboard.shared.domain.model.SupremeCourtDecision
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
class ApprovalModalViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo: FakeApprovalRepository
    private lateinit var viewModel: ApprovalModalViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeApprovalRepository()
        viewModel =
            ApprovalModalViewModel(
                respondToApprovalUseCase = RespondToApprovalUseCase(fakeRepo),
                nowMs = { testDispatcher.scheduler.currentTime },
            )
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    // --- Group 1: Initial State ---

    @Test
    fun `T1 initial state has no pending approval null remainingTimeSec not timed out not submitting`() {
        val state = viewModel.uiState.value
        assertNull(state.pendingApproval)
        assertNull(state.remainingTimeSec)
        assertFalse(state.isTimedOut)
        assertFalse(state.isSubmitting)
        assertNull(state.error)
    }

    @Test
    fun `T2 initial state showDialog is false`() {
        assertFalse(viewModel.uiState.value.showDialog)
    }

    // --- Group 2: Receiving Approval Events ---

    @Test
    fun `T3 onApprovalRequested with strategy type sets pendingApproval and showDialog true`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent())
            runCurrent()

            val state = viewModel.uiState.value
            assertNotNull(state.pendingApproval)
            assertEquals("strategy", state.pendingApproval?.approvalType)
            assertTrue(state.showDialog)
        }

    @Test
    fun `T4 onApprovalRequested with supreme court type sets correct approvalType`() =
        runTest {
            viewModel.onApprovalRequested(supremeCourtEvent())
            runCurrent()

            assertEquals("supreme_court", viewModel.uiState.value.pendingApproval?.approvalType)
        }

    @Test
    fun `T5 onApprovalRequested sets remainingTimeSec to event timeoutSec`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            assertEquals(60, viewModel.uiState.value.remainingTimeSec)
        }

    @Test
    fun `T6 onApprovalRequested uses DEFAULT_TIMEOUT_SEC 300 when event has no timeout`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = null))
            runCurrent()

            assertEquals(300, viewModel.uiState.value.remainingTimeSec)
        }

    @Test
    fun `T7 second onApprovalRequested is ignored while one is already pending`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(approvalId = "first", timeoutSec = 60))
            runCurrent()

            viewModel.onApprovalRequested(supremeCourtEvent(approvalId = "second", timeoutSec = 60))
            runCurrent()

            assertEquals("first", viewModel.uiState.value.pendingApproval?.id)
        }

    // --- Group 3: Countdown ---

    @Test
    fun `T8 countdown decrements remainingTimeSec every second`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 10))
            runCurrent()

            assertEquals(10, viewModel.uiState.value.remainingTimeSec)

            advanceTimeBy(3001)
            runCurrent()

            assertEquals(7, viewModel.uiState.value.remainingTimeSec)
        }

    @Test
    fun `T9 countdown reaching zero sets isTimedOut true`() =
        runTest {
            // No useCase: autoApprove() is a no-op, so the timed-out state persists for assertion
            val vm = ApprovalModalViewModel(respondToApprovalUseCase = null, nowMs = { testDispatcher.scheduler.currentTime })
            vm.onApprovalRequested(strategyEvent(timeoutSec = 3))
            advanceUntilIdle()

            assertEquals(0, vm.uiState.value.remainingTimeSec)
            assertTrue(vm.uiState.value.isTimedOut)

            vm.onCleared()
        }

    @Test
    fun `T10 countdown is deadline-based advancing 3 seconds yields correct remaining`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 30))
            runCurrent()

            advanceTimeBy(3000)
            runCurrent()

            val remaining = viewModel.uiState.value.remainingTimeSec ?: -1
            assertTrue(remaining in 26..27, "Expected ~27 seconds remaining, got $remaining")
        }

    // --- Group 4: Auto-Approve on Timeout ---

    @Test
    fun `T11 when countdown reaches zero auto-approve response is sent via repository`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(approvalId = "a1", timeoutSec = 3))
            advanceUntilIdle()

            assertEquals(1, fakeRepo.respondCallCount)
            assertEquals("a1", fakeRepo.lastApprovalId)
        }

    @Test
    fun `T12 auto-approve decision string is auto_approved`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 3))
            advanceUntilIdle()

            assertEquals("auto_approved", fakeRepo.lastDecision)
        }

    @Test
    fun `T13 after auto-approve pendingApproval is cleared and showDialog is false`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 3))
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.pendingApproval)
            assertFalse(viewModel.uiState.value.showDialog)
        }

    @Test
    fun `T13b auto-approve failure clears modal state and sets error`() =
        runTest {
            fakeRepo.respondResult = Result.failure(RuntimeException("Timeout network error"))
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 3))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.pendingApproval)
            assertFalse(state.showDialog)
            assertEquals("Timeout network error", state.error)
        }

    // --- Group 5: User Response ---

    @Test
    fun `T14 respond with StrategyDecision SplitExecute sends split_execute to repository`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(approvalId = "a1", timeoutSec = 60))
            runCurrent()

            viewModel.respond(StrategyDecision.SplitExecute)
            advanceUntilIdle()

            assertEquals("split_execute", fakeRepo.lastDecision)
            assertEquals("a1", fakeRepo.lastApprovalId)
        }

    @Test
    fun `T15 respond with StrategyDecision NoSplit sends no_split to repository`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(StrategyDecision.NoSplit)
            advanceUntilIdle()

            assertEquals("no_split", fakeRepo.lastDecision)
        }

    @Test
    fun `T16 respond with StrategyDecision Cancel sends cancel to repository`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(StrategyDecision.Cancel)
            advanceUntilIdle()

            assertEquals("cancel", fakeRepo.lastDecision)
        }

    @Test
    fun `T17 respond with SupremeCourtDecision Uphold sends uphold to repository`() =
        runTest {
            viewModel.onApprovalRequested(supremeCourtEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(SupremeCourtDecision.Uphold)
            advanceUntilIdle()

            assertEquals("uphold", fakeRepo.lastDecision)
        }

    @Test
    fun `T18 respond with SupremeCourtDecision Overturn sends overturn to repository`() =
        runTest {
            viewModel.onApprovalRequested(supremeCourtEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(SupremeCourtDecision.Overturn)
            advanceUntilIdle()

            assertEquals("overturn", fakeRepo.lastDecision)
        }

    @Test
    fun `T19 respond with SupremeCourtDecision Redesign sends redesign to repository`() =
        runTest {
            viewModel.onApprovalRequested(supremeCourtEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(SupremeCourtDecision.Redesign)
            advanceUntilIdle()

            assertEquals("redesign", fakeRepo.lastDecision)
        }

    @Test
    fun `T20 successful respond clears pendingApproval stops countdown showDialog false`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(StrategyDecision.SplitExecute)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.pendingApproval)
            assertNull(state.remainingTimeSec)
            assertFalse(state.showDialog)
        }

    @Test
    fun `T21 respond is ignored when isTimedOut is true`() =
        runTest {
            // No useCase: autoApprove() is a no-op, so isTimedOut stays true for the guard test
            val vm = ApprovalModalViewModel(respondToApprovalUseCase = null, nowMs = { testDispatcher.scheduler.currentTime })
            vm.onApprovalRequested(strategyEvent(timeoutSec = 3))
            advanceUntilIdle()

            assertTrue(vm.uiState.value.isTimedOut)
            val approvalBefore = vm.uiState.value.pendingApproval

            vm.respond(StrategyDecision.SplitExecute)
            advanceUntilIdle()

            // State is unchanged: respond() exited early due to isTimedOut guard
            assertEquals(approvalBefore, vm.uiState.value.pendingApproval)

            vm.onCleared()
        }

    @Test
    fun `T22 respond failure sets error in state approval remains pending`() =
        runTest {
            fakeRepo.respondResult = Result.failure(RuntimeException("Network error"))
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(StrategyDecision.SplitExecute)
            runCurrent() // don't advance time past timeout — only run the launched coroutine

            assertEquals("Network error", viewModel.uiState.value.error)
            assertNotNull(viewModel.uiState.value.pendingApproval)
        }

    @Test
    fun `T23 respond sets isSubmitting true during network call`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(StrategyDecision.SplitExecute)
            // isSubmitting is set synchronously before the coroutine launches — check before runCurrent
            assertTrue(viewModel.uiState.value.isSubmitting)
        }

    // --- Group 6: Dismiss ---

    @Test
    fun `T24 dismiss clears pendingApproval and remainingTimeSec showDialog false`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            assertNotNull(viewModel.uiState.value.pendingApproval)

            viewModel.dismiss()

            val state = viewModel.uiState.value
            assertNull(state.pendingApproval)
            assertNull(state.remainingTimeSec)
            assertFalse(state.showDialog)
        }

    @Test
    fun `T25 dismiss stops countdown job`() =
        runTest {
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            val remainingAfterStart = viewModel.uiState.value.remainingTimeSec

            viewModel.dismiss()

            advanceTimeBy(5000)
            runCurrent()

            assertNull(viewModel.uiState.value.remainingTimeSec)
        }

    // --- Group 7: Error Handling ---

    @Test
    fun `T26 clearError resets error to null`() =
        runTest {
            fakeRepo.respondResult = Result.failure(RuntimeException("err"))
            viewModel.onApprovalRequested(strategyEvent(timeoutSec = 60))
            runCurrent()

            viewModel.respond(StrategyDecision.SplitExecute)
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    // --- Additional: ApprovalContext mapping ---

    @Test
    fun `context from event is mapped into ApprovalRequest`() =
        runTest {
            viewModel.onApprovalRequested(
                supremeCourtEvent(
                    context = ApprovalContextDto(eta = "5m", detail = "Test ruling"),
                ),
            )
            runCurrent()

            val ctx = viewModel.uiState.value.pendingApproval?.context
            assertNotNull(ctx)
            assertEquals("5m", ctx.eta)
            assertEquals("Test ruling", ctx.detail)
        }

    @Test
    fun `GenericDecision sends its value string to repository`() =
        runTest {
            viewModel.onApprovalRequested(
                PipelineEventDto(
                    type = "approval.requested",
                    pipelineId = "p1",
                    approvalId = "g1",
                    approvalType = "generic",
                    options = listOf("approve", "reject"),
                    timeoutSec = 60,
                ),
            )
            runCurrent()

            viewModel.respond(GenericDecision("approve"))
            advanceUntilIdle()

            assertEquals("approve", fakeRepo.lastDecision)
        }

    // --- Helpers ---

    private fun strategyEvent(
        approvalId: String = "a1",
        timeoutSec: Int? = 60,
    ) = PipelineEventDto(
        type = "approval.requested",
        pipelineId = "p1",
        approvalId = approvalId,
        approvalType = "strategy",
        options = listOf("split_execute", "no_split", "cancel"),
        timeoutSec = timeoutSec,
    )

    private fun supremeCourtEvent(
        approvalId: String = "sc1",
        timeoutSec: Int? = 120,
        context: ApprovalContextDto? = null,
    ) = PipelineEventDto(
        type = "supreme_court.required",
        pipelineId = "p1",
        approvalId = approvalId,
        approvalType = "supreme_court",
        options = listOf("uphold", "overturn", "redesign"),
        context = context,
        timeoutSec = timeoutSec,
    )
}
