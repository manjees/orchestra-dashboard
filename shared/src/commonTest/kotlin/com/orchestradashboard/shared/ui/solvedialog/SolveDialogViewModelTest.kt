package com.orchestradashboard.shared.ui.solvedialog

import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.domain.model.SolveResponse
import com.orchestradashboard.shared.domain.usecase.ExecuteSolveUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SolveDialogViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSolveRepository: FakeSolveRepository
    private lateinit var viewModel: SolveDialogViewModel

    private val testProject =
        Project(
            name = "my-project",
            path = "/path",
            ciCommands = emptyList(),
            openIssuesCount = 1,
            recentSolves = 0,
        )

    private val testIssue =
        Issue(
            number = 42,
            title = "Fix auth bug",
            labels = emptyList(),
            state = "open",
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeSolveRepository = FakeSolveRepository()
        viewModel = SolveDialogViewModel(ExecuteSolveUseCase(fakeSolveRepository))
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is closed with defaults`() {
        val state = viewModel.uiState.value
        assertFalse(state.showDialog)
        assertNull(state.projectName)
        assertTrue(state.selectedIssues.isEmpty())
        assertEquals(SolveMode.AUTO, state.mode)
        assertFalse(state.isParallel)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.result)
    }

    @Test
    fun `open sets showDialog true and preselects issue`() =
        runTest {
            viewModel.open(testProject, testIssue)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.showDialog)
            assertEquals("my-project", state.projectName)
            assertEquals(setOf(42), state.selectedIssues)
            assertEquals(SolveMode.AUTO, state.mode)
            assertFalse(state.isParallel)
            assertNull(state.error)
        }

    @Test
    fun `toggleIssueSelection adds issue when absent`() =
        runTest {
            viewModel.toggleIssueSelection(5)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.selectedIssues.contains(5))
        }

    @Test
    fun `toggleIssueSelection removes issue when already selected`() =
        runTest {
            viewModel.toggleIssueSelection(5)
            viewModel.toggleIssueSelection(5)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.selectedIssues.contains(5))
        }

    @Test
    fun `setMode updates mode in state`() =
        runTest {
            viewModel.setMode(SolveMode.FULL)
            advanceUntilIdle()

            assertEquals(SolveMode.FULL, viewModel.uiState.value.mode)
        }

    @Test
    fun `toggleParallel flips isParallel flag`() =
        runTest {
            assertFalse(viewModel.uiState.value.isParallel)

            viewModel.toggleParallel()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isParallel)

            viewModel.toggleParallel()
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isParallel)
        }

    @Test
    fun `close resets dialog state`() =
        runTest {
            viewModel.open(testProject, testIssue)
            viewModel.setMode(SolveMode.FULL)
            viewModel.toggleParallel()
            viewModel.close()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.showDialog)
            assertNull(state.projectName)
            assertTrue(state.selectedIssues.isEmpty())
            assertEquals(SolveMode.AUTO, state.mode)
            assertFalse(state.isParallel)
            assertNull(state.error)
            assertNull(state.result)
        }

    @Test
    fun `executeSolve on success sets result and closes dialog`() =
        runTest {
            fakeSolveRepository.result = Result.success(SolveResponse("pipe-42", "started"))

            viewModel.open(testProject, testIssue)
            viewModel.executeSolve()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.result)
            assertEquals("pipe-42", state.result!!.pipelineId)
            assertFalse(state.showDialog)
            assertFalse(state.isLoading)
            assertNull(state.error)
        }

    @Test
    fun `executeSolve sends a SolveRequest with current selection mode and parallel`() =
        runTest {
            fakeSolveRepository.result = Result.success(SolveResponse("pipe-1", "started"))

            viewModel.open(testProject, testIssue)
            viewModel.toggleIssueSelection(7)
            viewModel.setMode(SolveMode.STANDARD)
            viewModel.toggleParallel()
            viewModel.executeSolve()
            advanceUntilIdle()

            val request = fakeSolveRepository.lastRequest
            assertNotNull(request)
            assertEquals("my-project", request!!.projectName)
            assertTrue(request.issueNumbers.containsAll(listOf(42, 7)))
            assertEquals(SolveMode.STANDARD, request.mode)
            assertTrue(request.parallel)
        }

    @Test
    fun `executeSolve on failure sets error message`() =
        runTest {
            fakeSolveRepository.result = Result.failure(RuntimeException("Solve failed"))

            viewModel.open(testProject, testIssue)
            viewModel.executeSolve()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Solve failed", state.error)
            assertFalse(state.isLoading)
            assertNull(state.result)
        }

    @Test
    fun `executeSolve does nothing when no issues selected`() =
        runTest {
            // open then deselect the preselected issue
            viewModel.open(testProject, testIssue)
            viewModel.toggleIssueSelection(testIssue.number)
            viewModel.executeSolve()
            advanceUntilIdle()

            assertEquals(0, fakeSolveRepository.executeSolveCallCount)
        }

    @Test
    fun `executeSolve does nothing when project is not set`() =
        runTest {
            // Skip open() — projectName remains null
            viewModel.toggleIssueSelection(1)
            viewModel.executeSolve()
            advanceUntilIdle()

            assertEquals(0, fakeSolveRepository.executeSolveCallCount)
        }

    @Test
    fun `consumeResult resets result to null`() =
        runTest {
            fakeSolveRepository.result = Result.success(SolveResponse("pipe-1", "started"))

            viewModel.open(testProject, testIssue)
            viewModel.executeSolve()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.result)

            viewModel.consumeResult()

            assertNull(viewModel.uiState.value.result)
        }

    @Test
    fun `clearError resets error to null`() =
        runTest {
            fakeSolveRepository.result = Result.failure(RuntimeException("boom"))

            viewModel.open(testProject, testIssue)
            viewModel.executeSolve()
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }
}
