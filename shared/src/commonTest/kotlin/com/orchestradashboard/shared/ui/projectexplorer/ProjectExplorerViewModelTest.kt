package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.domain.model.Project
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
        viewModel =
            ProjectExplorerViewModel(
                getProjectsUseCase = GetProjectsUseCase(projectRepository),
                getProjectIssuesUseCase = GetProjectIssuesUseCase(projectRepository),
                getCheckpointsUseCase = GetCheckpointsUseCase(repository),
                retryCheckpointUseCase = RetryCheckpointUseCase(repository),
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
}
