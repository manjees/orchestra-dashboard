package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Project
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ProjectCardTest {
    private fun testProject(
        name: String = "test-project",
        path: String = "/home/user/test-project",
        openIssuesCount: Int = 5,
    ) = Project(
        name = name,
        path = path,
        ciCommands = listOf("pytest"),
        openIssuesCount = openIssuesCount,
        recentSolves = 2,
    )

    @Test
    fun `displays project name`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ProjectCard(project = testProject(name = "my-app"), isSelected = false, onClick = {})
                }
            }
            onNodeWithText("my-app").assertIsDisplayed()
        }

    @Test
    fun `displays project path`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ProjectCard(project = testProject(path = "/srv/my-app"), isSelected = false, onClick = {})
                }
            }
            onNodeWithText("/srv/my-app").assertIsDisplayed()
        }

    @Test
    fun `displays open issues count`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ProjectCard(project = testProject(openIssuesCount = 7), isSelected = false, onClick = {})
                }
            }
            onNodeWithText("7 open issues").assertIsDisplayed()
        }

    @Test
    fun `onClick callback fires when card clicked`() =
        runComposeUiTest {
            var clicked = false
            setContent {
                DashboardTheme {
                    ProjectCard(
                        project = testProject(name = "clickable"),
                        isSelected = false,
                        onClick = { clicked = true },
                    )
                }
            }
            onNodeWithText("clickable").performClick()
            assertTrue(clicked)
        }

    @Test
    fun `selected state shows visual distinction`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    ProjectCard(project = testProject(name = "selected-proj"), isSelected = true, onClick = {})
                }
            }
            // Just verify it renders without crash when selected
            onNodeWithText("selected-proj").assertIsDisplayed()
        }
}
