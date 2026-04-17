package com.orchestradashboard.shared.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.model.SolveResponse
import com.orchestradashboard.shared.domain.repository.SolveRepository
import com.orchestradashboard.shared.domain.usecase.ExecuteSolveUseCase
import com.orchestradashboard.shared.domain.usecase.GetCheckpointsUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectIssuesUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectsUseCase
import com.orchestradashboard.shared.domain.usecase.RetryCheckpointUseCase
import com.orchestradashboard.shared.ui.projectexplorer.ProjectExplorerViewModel
import com.orchestradashboard.shared.ui.solvedialog.SolveDialogViewModel
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun createExplorerViewModel(
    projects: List<Project> = emptyList(),
    issues: Map<String, List<Issue>> = emptyMap(),
): ProjectExplorerViewModel {
    val repo = FakeProjectRepository(projects, issues)
    return ProjectExplorerViewModel(
        getProjectsUseCase = GetProjectsUseCase(repo),
        getProjectIssuesUseCase = GetProjectIssuesUseCase(repo),
        getCheckpointsUseCase = GetCheckpointsUseCase(repo),
        retryCheckpointUseCase = RetryCheckpointUseCase(repo),
    )
}

private val flowProject = Project("flow-project", "/home/flow", listOf("pytest"), 1, 0)
private val flowIssue = Issue(1, "Fix auth bug", listOf("bug"), "open", Instant.parse("2025-01-10T00:00:00Z"))

private val flowProjects = listOf(flowProject)

// Single issue avoids multiple "Solve" button ambiguity on issue row
private val flowIssuesMap = mapOf("flow-project" to listOf(flowIssue))

private val successSolveRepository =
    object : SolveRepository {
        override suspend fun executeSolve(request: SolveRequest): Result<SolveResponse> =
            Result.success(SolveResponse("pipe-success-123", "started"))
    }

private val failingSolveRepository =
    object : SolveRepository {
        override suspend fun executeSolve(request: SolveRequest): Result<SolveResponse> = Result.failure(Exception("Network error"))
    }

private fun createSolveFlowViewModel(repo: SolveRepository = successSolveRepository): SolveDialogViewModel =
    SolveDialogViewModel(ExecuteSolveUseCase(repo))

@OptIn(ExperimentalTestApi::class)
class ProjectExplorerScreenSolveFlowTest {
    @Test
    fun `opening dialog from IssueRow shows SolveDialog with that issue preselected`() =
        runComposeUiTest {
            val vm = createExplorerViewModel(projects = flowProjects, issues = flowIssuesMap)
            val solveVm = createSolveFlowViewModel()
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, solveDialogViewModel = solveVm, onBackClick = {})
                }
            }
            waitForIdle()
            onNodeWithText("flow-project").performClick()
            waitForIdle()
            // Single issue → one "Solve" button in the issue row
            onNodeWithText("Solve").performClick()
            waitForIdle()
            onNodeWithText("Solve Issues").assertIsDisplayed()
            // Issue #1 is preselected in the dialog state
            assertTrue(solveVm.uiState.value.selectedIssues.contains(1))
        }

    @Test
    fun `successful solve triggers onNavigateToPipeline with returned pipelineId`() =
        runComposeUiTest {
            val vm = createExplorerViewModel(projects = flowProjects, issues = flowIssuesMap)
            val solveVm = createSolveFlowViewModel(successSolveRepository)
            var navigatedPipelineId: String? = null
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(
                        viewModel = vm,
                        solveDialogViewModel = solveVm,
                        onBackClick = {},
                        onNavigateToPipeline = { navigatedPipelineId = it },
                    )
                }
            }
            waitForIdle()
            // Open the dialog directly via the VM to avoid button ambiguity
            solveVm.open(flowProject, flowIssue)
            waitForIdle()
            onNodeWithText("Solve Issues").assertIsDisplayed()
            // Click dialog's confirm Solve button (tagged)
            onNodeWithTag("solve_confirm_button").performClick()
            waitForIdle()
            assertEquals("pipe-success-123", navigatedPipelineId)
        }

    @Test
    fun `after successful solve the dialog is dismissed`() =
        runComposeUiTest {
            val vm = createExplorerViewModel(projects = flowProjects, issues = flowIssuesMap)
            val solveVm = createSolveFlowViewModel(successSolveRepository)
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, solveDialogViewModel = solveVm, onBackClick = {})
                }
            }
            waitForIdle()
            solveVm.open(flowProject, flowIssue)
            waitForIdle()
            onNodeWithText("Solve Issues").assertIsDisplayed()
            onNodeWithTag("solve_confirm_button").performClick()
            waitForIdle()
            // Dialog should be dismissed and result consumed
            assertTrue(!solveVm.uiState.value.showDialog)
            assertNull(solveVm.uiState.value.result)
        }

    @Test
    fun `solve failure shows error message in dialog`() =
        runComposeUiTest {
            val vm = createExplorerViewModel(projects = flowProjects, issues = flowIssuesMap)
            val solveVm = createSolveFlowViewModel(failingSolveRepository)
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, solveDialogViewModel = solveVm, onBackClick = {})
                }
            }
            waitForIdle()
            solveVm.open(flowProject, flowIssue)
            waitForIdle()
            onNodeWithText("Solve Issues").assertIsDisplayed()
            onNodeWithTag("solve_confirm_button").performClick()
            waitForIdle()
            // Error message visible inside the dialog
            onNodeWithText("Network error").assertIsDisplayed()
        }

    @Test
    fun `clearError removes error from state`() =
        runComposeUiTest {
            val vm = createExplorerViewModel(projects = flowProjects, issues = flowIssuesMap)
            val solveVm = createSolveFlowViewModel(failingSolveRepository)
            setContent {
                DashboardTheme {
                    ProjectExplorerScreen(viewModel = vm, solveDialogViewModel = solveVm, onBackClick = {})
                }
            }
            waitForIdle()
            solveVm.open(flowProject, flowIssue)
            waitForIdle()
            onNodeWithTag("solve_confirm_button").performClick()
            waitForIdle()
            onNodeWithText("Network error").assertIsDisplayed()
            // Dismiss via clearError
            solveVm.clearError()
            waitForIdle()
            assertNull(solveVm.uiState.value.error)
        }
}
