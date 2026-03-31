package com.orchestradashboard.shared.ui.projectexplorer

import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.usecase.GetProjectIssuesUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectsUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectExplorerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeProjectRepository
    private lateinit var viewModel: ProjectExplorerViewModel

    private val testProjects =
        listOf(
            Project("project-alpha", "/path/alpha", 5, 2),
            Project("project-beta", "/path/beta", 0, 0),
            Project("project-gamma", "/path/gamma", 3, 1),
        )

    private val testIssues =
        listOf(
            Issue(1, "Fix login bug", listOf("bug"), "open", "2024-01-15T10:00:00Z"),
            Issue(2, "Add feature X", listOf("enhancement"), "open", "2024-01-16T10:00:00Z"),
        )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeProjectRepository()
        viewModel = ProjectExplorerViewModel(
            GetProjectsUseCase(fakeRepository),
            GetProjectIssuesUseCase(fakeRepository),
        )
    }

    @AfterTest
    fun teardown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    // --- Initial State ---

    @Test
    fun `initial state has empty projects no selectedProject no issues not loading`() {
        val state = viewModel.uiState.value

        assertTrue(state.projects.isEmpty())
        assertNull(state.selectedProject)
        assertTrue(state.issues.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isLoadingIssues)
        assertNull(state.error)
    }

    // --- Project Loading ---

    @Test
    fun `loadProjects sets isLoading then populates projects list`() =
        runTest {
            fakeRepository.projectsResult = Result.success(testProjects)

            viewModel.loadProjects()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(3, state.projects.size)
            assertEquals("project-alpha", state.projects[0].name)
        }

    @Test
    fun `loadProjects sets error on failure and clears isLoading`() =
        runTest {
            fakeRepository.projectsResult = Result.failure(RuntimeException("Network error"))

            viewModel.loadProjects()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Network error"))
        }

    @Test
    fun `loadProjects with empty result keeps projects empty without error`() =
        runTest {
            fakeRepository.projectsResult = Result.success(emptyList())

            viewModel.loadProjects()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.projects.isEmpty())
            assertNull(state.error)
        }

    // --- Project Selection ---

    @Test
    fun `selectProject sets selectedProject and triggers issue loading`() =
        runTest {
            fakeRepository.issuesResult = Result.success(testIssues)
            val project = testProjects[0]

            viewModel.selectProject(project)
            advanceUntilIdle()

            assertEquals(project, viewModel.uiState.value.selectedProject)
            assertEquals(1, fakeRepository.getProjectIssuesCallCount)
            assertEquals("project-alpha", fakeRepository.lastRequestedProjectName)
        }

    @Test
    fun `selectProject loads issues for the selected project`() =
        runTest {
            fakeRepository.issuesResult = Result.success(testIssues)

            viewModel.selectProject(testProjects[0])
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(2, state.issues.size)
            assertEquals("Fix login bug", state.issues[0].title)
        }

    @Test
    fun `selectProject clears previous issues before loading new ones`() =
        runTest {
            fakeRepository.issuesResult = Result.success(testIssues)
            viewModel.selectProject(testProjects[0])
            advanceUntilIdle()
            assertEquals(2, viewModel.uiState.value.issues.size)

            fakeRepository.issuesResult =
                Result.success(
                    listOf(
                        Issue(10, "Different issue", emptyList(), "open", "2024-03-01T00:00:00Z"),
                    ),
                )
            viewModel.selectProject(testProjects[1])
            advanceUntilIdle()

            assertEquals(1, viewModel.uiState.value.issues.size)
            assertEquals("Different issue", viewModel.uiState.value.issues[0].title)
        }

    @Test
    fun `selectProject with null deselects project and clears issues`() =
        runTest {
            fakeRepository.issuesResult = Result.success(testIssues)
            viewModel.selectProject(testProjects[0])
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.selectedProject)

            viewModel.selectProject(null)

            assertNull(viewModel.uiState.value.selectedProject)
            assertTrue(viewModel.uiState.value.issues.isEmpty())
        }

    // --- Issue Loading ---

    @Test
    fun `issues load successfully for selected project`() =
        runTest {
            fakeRepository.issuesResult = Result.success(testIssues)

            viewModel.selectProject(testProjects[0])
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoadingIssues)
            assertEquals(2, state.issues.size)
        }

    @Test
    fun `issue loading failure sets error but keeps selectedProject`() =
        runTest {
            fakeRepository.issuesResult = Result.failure(RuntimeException("API error"))

            viewModel.selectProject(testProjects[0])
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.selectedProject)
            assertNotNull(state.error)
            assertTrue(state.issues.isEmpty())
        }

    // --- Refresh ---

    @Test
    fun `refresh reloads projects list`() =
        runTest {
            fakeRepository.projectsResult = Result.success(testProjects)

            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(3, viewModel.uiState.value.projects.size)
            assertEquals(1, fakeRepository.getProjectsCallCount)
        }

    @Test
    fun `refresh with selectedProject also reloads issues`() =
        runTest {
            fakeRepository.projectsResult = Result.success(testProjects)
            fakeRepository.issuesResult = Result.success(testIssues)
            viewModel.selectProject(testProjects[0])
            advanceUntilIdle()

            viewModel.refresh()
            advanceUntilIdle()

            assertEquals(2, fakeRepository.getProjectIssuesCallCount)
            assertEquals(1, fakeRepository.getProjectsCallCount)
        }

    // --- Error Handling ---

    @Test
    fun `clearError resets error to null`() =
        runTest {
            fakeRepository.projectsResult = Result.failure(RuntimeException("error"))
            viewModel.loadProjects()
            advanceUntilIdle()
            assertNotNull(viewModel.uiState.value.error)

            viewModel.clearError()

            assertNull(viewModel.uiState.value.error)
        }

    // --- Lifecycle ---

    @Test
    fun `onCleared cancels all coroutines`() =
        runTest {
            viewModel.onCleared()

            fakeRepository.projectsResult = Result.success(testProjects)
            viewModel.loadProjects()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.projects.isEmpty())
        }
}
