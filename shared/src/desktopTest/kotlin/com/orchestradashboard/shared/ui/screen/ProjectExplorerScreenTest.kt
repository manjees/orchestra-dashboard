package com.orchestradashboard.shared.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.repository.ProjectRepository
import com.orchestradashboard.shared.domain.usecase.GetCheckpointsUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectIssuesUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectsUseCase
import com.orchestradashboard.shared.ui.projectexplorer.ProjectExplorerViewModel
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

class FakeProjectRepository(
    private val projects: List<Project> = emptyList(),
    private val issues: Map<String, List<Issue>> = emptyMap(),
    private val checkpoints: List<Checkpoint> = emptyList(),
) : ProjectRepository {
    override suspend fun getProjects(): Result<List<Project>> = Result.success(projects)

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): Result<List<Issue>> = Result.success(issues[name] ?: emptyList())

    override suspend fun getCheckpoints(): Result<List<Checkpoint>> = Result.success(checkpoints)
}

private fun createViewModel(
    projects: List<Project> = emptyList(),
    issues: Map<String, List<Issue>> = emptyMap(),
    checkpoints: List<Checkpoint> = emptyList(),
): ProjectExplorerViewModel {
    val repo = FakeProjectRepository(projects, issues, checkpoints)
    return ProjectExplorerViewModel(
        getProjectsUseCase = GetProjectsUseCase(repo),
        getProjectIssuesUseCase = GetProjectIssuesUseCase(repo),
        getCheckpointsUseCase = GetCheckpointsUseCase(repo),
    )
}

private val sampleProjects =
    listOf(
        Project("project-alpha", "/home/alpha", listOf("pytest"), 3, 1),
        Project("project-beta", "/home/beta", emptyList(), 0, 0),
    )

private val sampleIssues =
    mapOf(
        "project-alpha" to
            listOf(
                Issue(1, "Fix auth", listOf("bug"), "open", Instant.parse("2025-01-10T00:00:00Z")),
                Issue(2, "Add feature", listOf("enhancement"), "open", Instant.parse("2025-01-11T00:00:00Z")),
            ),
    )

private val sampleCheckpoints =
    listOf(
        Checkpoint("cp-1", "pipe-1", Instant.parse("2025-03-01T00:00:00Z"), "lint", CheckpointStatus.FAILED),
    )

@OptIn(ExperimentalTestApi::class)
class ProjectExplorerScreenTest {
    @Test
    fun `displays toolbar title`() =
        runComposeUiTest {
            val vm = createViewModel()
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, onBackClick = {})
                }
            }
            onNodeWithText("Project Explorer").assertIsDisplayed()
        }

    @Test
    fun `displays empty state when no projects`() =
        runComposeUiTest {
            val vm = createViewModel()
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("No projects registered.").assertIsDisplayed()
        }

    @Test
    fun `displays project cards when projects exist`() =
        runComposeUiTest {
            val vm = createViewModel(projects = sampleProjects)
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("project-alpha").assertIsDisplayed()
            onNodeWithText("project-beta").assertIsDisplayed()
        }

    @Test
    fun `selecting project shows issue list`() =
        runComposeUiTest {
            val vm = createViewModel(projects = sampleProjects, issues = sampleIssues)
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("project-alpha").performClick()
            waitForIdle()
            onNodeWithText("#1 Fix auth").assertIsDisplayed()
            onNodeWithText("#2 Add feature").assertIsDisplayed()
        }

    @Test
    fun `displays checkpoints section`() =
        runComposeUiTest {
            val vm = createViewModel(projects = sampleProjects, checkpoints = sampleCheckpoints)
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("Checkpoints").assertIsDisplayed()
            onNodeWithText("lint — failed").assertIsDisplayed()
        }

    @Test
    fun `displays empty checkpoint state when none exist`() =
        runComposeUiTest {
            val vm = createViewModel(projects = sampleProjects)
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("No checkpoints").assertIsDisplayed()
        }

    @Test
    fun `back button callback fires`() =
        runComposeUiTest {
            var backClicked = false
            val vm = createViewModel()
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, onBackClick = { backClicked = true })
                }
            }
            onNodeWithText("Back").assertDoesNotExist() // It's an icon, use content description
            // Click the back icon button
            onNodeWithContentDescription("Back").performClick()
            assertTrue(backClicked)
        }

    @Test
    fun `refresh button triggers reload`() =
        runComposeUiTest {
            val vm = createViewModel(projects = sampleProjects)
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("project-alpha").assertIsDisplayed()
            // Click refresh
            onNodeWithContentDescription("Refresh").performClick()
            waitForIdle()
            // Should still display projects after refresh
            onNodeWithText("project-alpha").assertIsDisplayed()
        }
}
