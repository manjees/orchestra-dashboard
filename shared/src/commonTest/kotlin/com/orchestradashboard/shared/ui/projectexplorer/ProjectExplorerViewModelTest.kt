package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
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
class ProjectExplorerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeCheckpointRepository
    private lateinit var projectRepository: FakeProjectRepository
    private lateinit var fakeSolveRepository: FakeSolveRepository
    private lateinit var viewModel: ProjectExplorerViewModel

    private val testCheckpoints =
        listOf(
            Checkpoint("cp-1", "pipeline-1", Instant.parse("2024-01-01T00:00:00Z"), "build", CheckpointStatus.FAILED),
            Checkpoint("cp-2", "pipeline-2", Instant.parse("2024-01-01T01:00:00Z"), "test", CheckpointStatus.FAILED),
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeCheckpointRepository()
        projectRepository = FakeProjectRepository()
        fakeSolveRepository = FakeSolveRepository()
        viewModel =
            ProjectExplorerViewModel(
                getProjectsUseCase = GetProjectsUseCase(projectRepository),
                getProjectIssuesUseCase = GetProjectIssuesUseCase(projectRepository),
                getCheckpointsUseCase = GetCheckpointsUseCase(repository),
                retryCheckpointUseCase = RetryCheckpointUseCase(repository),
                executeSolveUseCase = ExecuteSolveUseCase(fakeSolveRepository),
            )
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun `loadInitialData loads both projects and checkpoints`() =
        runTest {
            val projects = listOf(Project("p1", "/path/1", emptyList(), 1, 0))
            projectRepository.projectsResult = Result.success(projects)
            repository.getFailedCheckpointsResult = Result.success(testCheckpoints)

            viewModel.loadInitialData()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(1, state.projects.size)
            assertEquals(2, state.checkpoints.size)
            assertFalse(state.isLoading)
        }

    @Test
    fun `retryCheckpoint sets retryingCheckpointId and then success result`() =
        runTest {
            val retriedCheckpoint = testCheckpoints[0].copy(status = CheckpointStatus.RUNNING)
            repository.retryCheckpointResult = Result.success(retriedCheckpoint)
            repository.getFailedCheckpointsResult = Result.success(testCheckpoints)

            viewModel.retryCheckpoint("cp-1")

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.retryingCheckpointId)
            assertNotNull(state.retryResult)
            assertTrue(state.retryResult!!.isSuccess)
        }

    @Test
    fun `retryCheckpoint failure sets retryResult with error`() =
        runTest {
            repository.retryCheckpointResult = Result.failure(RuntimeException("Retry failed"))

            viewModel.retryCheckpoint("cp-1")
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNull(state.retryingCheckpointId)
            assertNotNull(state.retryResult)
            assertTrue(state.retryResult!!.isFailure)
        }

    @Test
    fun `openSolveDialog sets showSolveDialog true with issue pre-selected`() =
        runTest {
            val issue = Issue(42, "Fix auth bug", emptyList(), "open", Instant.parse("2024-01-01T00:00:00Z"))
            viewModel.openSolveDialog(issue)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.showSolveDialog)
            assertEquals(setOf(42), state.selectedIssues)
            assertEquals(SolveMode.AUTO, state.solveMode)
            assertFalse(state.isParallel)
            assertNull(state.solveError)
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
    fun `setSolveMode updates solveMode in state`() =
        runTest {
            viewModel.setSolveMode(SolveMode.FULL)
            advanceUntilIdle()

            assertEquals(SolveMode.FULL, viewModel.uiState.value.solveMode)
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
    fun `closeSolveDialog resets dialog state`() =
        runTest {
            val issue = Issue(1, "Test issue", emptyList(), "open", Instant.parse("2024-01-01T00:00:00Z"))
            viewModel.openSolveDialog(issue)
            viewModel.closeSolveDialog()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.showSolveDialog)
            assertTrue(state.selectedIssues.isEmpty())
            assertEquals(SolveMode.AUTO, state.solveMode)
            assertFalse(state.isParallel)
            assertNull(state.solveError)
            assertNull(state.solveResult)
        }

    @Test
    fun `executeSolve on success sets solveResult and closes dialog`() =
        runTest {
            fakeSolveRepository.result = Result.success(SolveResponse("pipe-42", "started"))
            val project = Project("my-project", "/path", emptyList(), 1, 0)
            projectRepository.projectsResult = Result.success(listOf(project))
            projectRepository.issuesResult = Result.success(emptyList())

            viewModel.loadInitialData()
            advanceUntilIdle()
            viewModel.selectProject(project)
            advanceUntilIdle()
            viewModel.toggleIssueSelection(1)
            viewModel.executeSolve()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.solveResult)
            assertEquals("pipe-42", state.solveResult!!.pipelineId)
            assertFalse(state.showSolveDialog)
            assertFalse(state.isSolving)
            assertNull(state.solveError)
        }

    @Test
    fun `executeSolve on failure sets solveError`() =
        runTest {
            fakeSolveRepository.result = Result.failure(RuntimeException("Solve failed"))
            val project = Project("my-project", "/path", emptyList(), 1, 0)
            projectRepository.projectsResult = Result.success(listOf(project))
            projectRepository.issuesResult = Result.success(emptyList())

            viewModel.loadInitialData()
            advanceUntilIdle()
            viewModel.selectProject(project)
            advanceUntilIdle()
            viewModel.toggleIssueSelection(1)
            viewModel.executeSolve()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Solve failed", state.solveError)
            assertFalse(state.isSolving)
            assertNull(state.solveResult)
        }
}

private class FakeSolveRepository : SolveRepository {
    var result: Result<SolveResponse> = Result.success(SolveResponse("pipe-noop", "started"))

    override suspend fun executeSolve(request: SolveRequest): Result<SolveResponse> = result
}
