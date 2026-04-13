package com.orchestradashboard.shared.ui.screen

import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.model.SolveResponse
import com.orchestradashboard.shared.domain.repository.SolveRepository
import com.orchestradashboard.shared.domain.usecase.ExecuteSolveUseCase
import com.orchestradashboard.shared.domain.usecase.GetCheckpointsUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectIssuesUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectsUseCase
import com.orchestradashboard.shared.domain.usecase.RetryCheckpointUseCase
import com.orchestradashboard.shared.ui.projectexplorer.ProjectExplorerViewModel
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

private class FakeSolveRepository(
    private val response: Result<SolveResponse> = Result.success(SolveResponse("pipe-1", "started")),
) : SolveRepository {
    var lastRequest: SolveRequest? = null

    override suspend fun executeSolve(request: SolveRequest): Result<SolveResponse> {
        lastRequest = request
        return response
    }
}

private val sampleIssue = Issue(1, "Fix auth bug", listOf("bug"), "open", Instant.parse("2025-01-10T00:00:00Z"))
private val sampleIssue2 = Issue(2, "Add feature", listOf("enhancement"), "open", Instant.parse("2025-01-11T00:00:00Z"))
private val sampleProject = Project("my-project", "/home/proj", emptyList(), 2, 0)

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectExplorerSolveTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeSolveRepo: FakeSolveRepository
    private lateinit var fakeProjectRepo: FakeProjectRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeSolveRepo = FakeSolveRepository()
        fakeProjectRepo =
            FakeProjectRepository(
                projects = listOf(sampleProject),
                issues = mapOf("my-project" to listOf(sampleIssue, sampleIssue2)),
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(solveRepo: FakeSolveRepository = fakeSolveRepo): ProjectExplorerViewModel {
        return ProjectExplorerViewModel(
            getProjectsUseCase = GetProjectsUseCase(fakeProjectRepo),
            getProjectIssuesUseCase = GetProjectIssuesUseCase(fakeProjectRepo),
            getCheckpointsUseCase = GetCheckpointsUseCase(fakeProjectRepo),
            retryCheckpointUseCase = RetryCheckpointUseCase(fakeProjectRepo),
            executeSolveUseCase = ExecuteSolveUseCase(solveRepo),
        )
    }

    @Test
    fun `openSolveDialog sets showSolveDialog true with selected issue`() =
        runTest {
            val vm = createViewModel()
            vm.openSolveDialog(sampleIssue)
            val state = vm.uiState.value
            assertTrue(state.showSolveDialog)
            assertTrue(state.selectedIssues.contains(1))
        }

    @Test
    fun `toggleIssueSelection adds issue when not selected`() =
        runTest {
            val vm = createViewModel()
            vm.openSolveDialog(sampleIssue)
            vm.toggleIssueSelection(2)
            assertTrue(vm.uiState.value.selectedIssues.contains(2))
        }

    @Test
    fun `toggleIssueSelection removes issue when already selected`() =
        runTest {
            val vm = createViewModel()
            vm.openSolveDialog(sampleIssue)
            vm.toggleIssueSelection(1)
            assertFalse(vm.uiState.value.selectedIssues.contains(1))
        }

    @Test
    fun `setSolveMode updates solveMode in state`() =
        runTest {
            val vm = createViewModel()
            vm.setSolveMode(SolveMode.EXPRESS)
            assertEquals(SolveMode.EXPRESS, vm.uiState.value.solveMode)
        }

    @Test
    fun `toggleParallel flips isParallel in state`() =
        runTest {
            val vm = createViewModel()
            assertFalse(vm.uiState.value.isParallel)
            vm.toggleParallel()
            assertTrue(vm.uiState.value.isParallel)
            vm.toggleParallel()
            assertFalse(vm.uiState.value.isParallel)
        }

    @Test
    fun `default solveMode is AUTO`() =
        runTest {
            val vm = createViewModel()
            assertEquals(SolveMode.AUTO, vm.uiState.value.solveMode)
        }

    @Test
    fun `executeSolve calls repository with correct SolveRequest`() =
        runTest {
            val vm = createViewModel()
            vm.uiState.value.let { } // ensure state is initialized

            // Set up state
            vm.openSolveDialog(sampleIssue)
            vm.toggleIssueSelection(2)
            vm.setSolveMode(SolveMode.STANDARD)
            vm.toggleParallel()

            // Need selectedProject set — simulate by loading data and selecting project
            // Since we don't have full VM flow here, test with just the repo call
            // Set selected project manually by loading initial data
            vm.loadInitialData()
            advanceUntilIdle()
            vm.selectProject(sampleProject)
            advanceUntilIdle()

            // Re-open dialog after project selection
            vm.openSolveDialog(sampleIssue)
            vm.toggleIssueSelection(2)
            vm.setSolveMode(SolveMode.STANDARD)
            vm.toggleParallel()

            vm.executeSolve()
            advanceUntilIdle()

            val request = fakeSolveRepo.lastRequest
            assertNotNull(request)
            assertEquals("my-project", request.projectName)
            assertTrue(request.issueNumbers.contains(1))
            assertEquals(SolveMode.STANDARD, request.mode)
            assertTrue(request.parallel)
        }

    @Test
    fun `executeSolve success sets solveResult and closes dialog`() =
        runTest {
            val vm = createViewModel()
            vm.loadInitialData()
            advanceUntilIdle()
            vm.selectProject(sampleProject)
            advanceUntilIdle()
            vm.openSolveDialog(sampleIssue)

            vm.executeSolve()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertNotNull(state.solveResult)
            assertEquals("pipe-1", state.solveResult!!.pipelineId)
            assertFalse(state.showSolveDialog)
        }

    @Test
    fun `executeSolve failure sets solveError in state`() =
        runTest {
            val failRepo = FakeSolveRepository(Result.failure(Exception("Network error")))
            val vm = createViewModel(failRepo)
            vm.loadInitialData()
            advanceUntilIdle()
            vm.selectProject(sampleProject)
            advanceUntilIdle()
            vm.openSolveDialog(sampleIssue)

            vm.executeSolve()
            advanceUntilIdle()

            val state = vm.uiState.value
            assertNotNull(state.solveError)
            assertEquals("Network error", state.solveError)
            assertNull(state.solveResult)
        }

    @Test
    fun `executeSolve sets isSolving true during execution`() =
        runTest {
            val vm = createViewModel()
            vm.loadInitialData()
            advanceUntilIdle()
            vm.selectProject(sampleProject)
            advanceUntilIdle()
            vm.openSolveDialog(sampleIssue)

            vm.executeSolve()
            // Before advanceUntilIdle, isSolving should be true
            assertTrue(vm.uiState.value.isSolving)
            advanceUntilIdle()
            assertFalse(vm.uiState.value.isSolving)
        }

    @Test
    fun `closeSolveDialog resets all dialog state`() =
        runTest {
            val vm = createViewModel()
            vm.openSolveDialog(sampleIssue)
            vm.setSolveMode(SolveMode.EXPRESS)
            vm.toggleParallel()

            vm.closeSolveDialog()

            val state = vm.uiState.value
            assertFalse(state.showSolveDialog)
            assertTrue(state.selectedIssues.isEmpty())
            assertEquals(SolveMode.AUTO, state.solveMode)
            assertFalse(state.isParallel)
            assertNull(state.solveError)
            assertNull(state.solveResult)
        }

    @Test
    fun `executeSolve does nothing when no issues selected`() =
        runTest {
            val vm = createViewModel()
            vm.loadInitialData()
            advanceUntilIdle()
            vm.selectProject(sampleProject)
            advanceUntilIdle()
            vm.openSolveDialog(sampleIssue)
            vm.toggleIssueSelection(1) // deselect all

            vm.executeSolve()
            advanceUntilIdle()

            assertNull(fakeSolveRepo.lastRequest)
            assertFalse(vm.uiState.value.isSolving)
        }
}
