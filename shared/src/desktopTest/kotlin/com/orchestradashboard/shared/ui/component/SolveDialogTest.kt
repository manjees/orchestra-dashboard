package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val issue1 = Issue(1, "Fix auth bug", listOf("bug"), "open", Instant.parse("2025-01-10T00:00:00Z"))
private val issue2 = Issue(2, "Add dark mode", listOf("enhancement"), "open", Instant.parse("2025-01-11T00:00:00Z"))

@OptIn(ExperimentalTestApi::class)
class SolveDialogTest {
    @Test
    fun `displays dialog title Solve Issues`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("Solve Issues").assertIsDisplayed()
        }

    @Test
    fun `displays issue checkboxes with number and title`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1, issue2),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("#1 Fix auth bug").assertIsDisplayed()
            onNodeWithText("#2 Add dark mode").assertIsDisplayed()
        }

    @Test
    fun `checking issue toggles selection state`() =
        runComposeUiTest {
            var toggled = -1
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = { toggled = it },
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("#1 Fix auth bug").performClick()
            assertEquals(1, toggled)
        }

    @Test
    fun `displays mode selector with 4 options`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("Express").assertIsDisplayed()
            onNodeWithText("Standard").assertIsDisplayed()
            onNodeWithText("Full").assertIsDisplayed()
            onNodeWithText("Auto").assertIsDisplayed()
        }

    @Test
    fun `parallel toggle hidden when single issue selected`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithTag("parallel_toggle").assertDoesNotExist()
        }

    @Test
    fun `parallel toggle shown when multiple issues selected`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1, issue2),
                        selectedIssues = setOf(1, 2),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithTag("parallel_toggle").assertIsDisplayed()
        }

    @Test
    fun `Solve button displays Solve text`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("Solve").assertIsDisplayed()
        }

    @Test
    fun `Solve button disabled when no issues checked`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = emptySet(),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("Solve").assertIsNotEnabled()
        }

    @Test
    fun `Solve button click triggers onSolve callback`() =
        runComposeUiTest {
            var solveCalled = false
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = { solveCalled = true },
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("Solve").performClick()
            assertTrue(solveCalled)
        }

    @Test
    fun `Cancel button dismisses dialog`() =
        runComposeUiTest {
            var dismissed = false
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = { dismissed = true },
                    )
                }
            }
            onNodeWithText("Cancel").performClick()
            assertTrue(dismissed)
        }

    @Test
    fun `loading indicator shown when isSolving true`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = true,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithTag("solve_loading_indicator").assertIsDisplayed()
        }

    @Test
    fun `error message displayed on solve failure`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = false,
                        solveError = "Network error occurred",
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("Network error occurred").assertIsDisplayed()
        }

    @Test
    fun `Solve button disabled when isSolving is true`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    SolveDialog(
                        issues = listOf(issue1),
                        selectedIssues = setOf(1),
                        solveMode = SolveMode.AUTO,
                        isParallel = false,
                        isSolving = true,
                        solveError = null,
                        onToggleIssue = {},
                        onModeChange = {},
                        onToggleParallel = {},
                        onSolve = {},
                        onDismiss = {},
                    )
                }
            }
            onNodeWithText("Solve").assertIsNotEnabled()
        }
}
