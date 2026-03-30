package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Issue
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class IssueRowTest {
    private fun testIssue(
        number: Int = 42,
        title: String = "Fix login bug",
        labels: List<String> = listOf("bug"),
    ) = Issue(
        number = number,
        title = title,
        labels = labels,
        state = "open",
        createdAt = Instant.parse("2025-01-15T10:30:00Z"),
    )

    @Test
    fun `displays issue number and title`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    IssueRow(issue = testIssue(number = 42, title = "Fix login bug"), onSolveClick = {})
                }
            }
            onNodeWithText("#42 Fix login bug").assertIsDisplayed()
        }

    @Test
    fun `displays labels as chips`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    IssueRow(
                        issue = testIssue(labels = listOf("bug", "priority:high")),
                        onSolveClick = {},
                    )
                }
            }
            onNodeWithText("bug").assertIsDisplayed()
            onNodeWithText("priority:high").assertIsDisplayed()
        }

    @Test
    fun `displays formatted creation date`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    IssueRow(issue = testIssue(), onSolveClick = {})
                }
            }
            onNodeWithText("2025-01-15").assertIsDisplayed()
        }

    @Test
    fun `Solve button is displayed`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    IssueRow(issue = testIssue(), onSolveClick = {})
                }
            }
            onNodeWithText("Solve").assertIsDisplayed()
        }

    @Test
    fun `Solve button onClick callback fires`() =
        runComposeUiTest {
            var clicked = false
            setContent {
                DashboardTheme {
                    IssueRow(issue = testIssue(), onSolveClick = { clicked = true })
                }
            }
            onNodeWithText("Solve").performClick()
            assertTrue(clicked)
        }
}
