package com.orchestradashboard.shared.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.AgentDetailUiState
import com.orchestradashboard.shared.domain.model.DetailTab
import com.orchestradashboard.shared.ui.TestAgentFactory
import com.orchestradashboard.shared.ui.TestEventFactory
import com.orchestradashboard.shared.ui.TestPipelineRunFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class AgentDetailScreenTest {
    @Test
    fun `should display agent name in top bar`() =
        runComposeUiTest {
            val state =
                AgentDetailUiState(
                    agent = TestAgentFactory.create(name = "my-agent"),
                )
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = {},
                    )
                }
            }
            onAllNodesWithText("my-agent")[0].assertIsDisplayed()
        }

    @Test
    fun `should display all three tabs`() =
        runComposeUiTest {
            val state = AgentDetailUiState(agent = TestAgentFactory.create())
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = {},
                    )
                }
            }
            onNodeWithText("Overview").assertIsDisplayed()
            onNodeWithText("Pipelines").assertIsDisplayed()
            onNodeWithText("Events").assertIsDisplayed()
        }

    @Test
    fun `should show overview tab selected by default`() =
        runComposeUiTest {
            val state =
                AgentDetailUiState(
                    agent = TestAgentFactory.create(),
                    selectedTab = DetailTab.OVERVIEW,
                )
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = {},
                    )
                }
            }
            onNodeWithText("Overview").assertIsSelected()
        }

    @Test
    fun `should invoke onTabSelected when tab is clicked`() =
        runComposeUiTest {
            var selectedTab: DetailTab? = null
            val state = AgentDetailUiState(agent = TestAgentFactory.create())
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = { selectedTab = it },
                        onBackClick = {},
                    )
                }
            }
            onNodeWithText("Pipelines").performClick()
            assertEquals(DetailTab.PIPELINES, selectedTab)
        }

    @Test
    fun `should show overview content when overview tab is selected`() =
        runComposeUiTest {
            val state =
                AgentDetailUiState(
                    agent = TestAgentFactory.create(name = "overview-agent"),
                    selectedTab = DetailTab.OVERVIEW,
                )
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = {},
                    )
                }
            }
            onAllNodesWithText("overview-agent")[0].assertIsDisplayed()
        }

    @Test
    fun `should show pipeline list when pipelines tab is selected`() =
        runComposeUiTest {
            val state =
                AgentDetailUiState(
                    agent = TestAgentFactory.create(),
                    pipelineRuns = TestPipelineRunFactory.createList(),
                    selectedTab = DetailTab.PIPELINES,
                )
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = {},
                    )
                }
            }
            onNodeWithText("deploy-pipeline").assertIsDisplayed()
        }

    @Test
    fun `should show event feed when events tab is selected`() =
        runComposeUiTest {
            val state =
                AgentDetailUiState(
                    agent = TestAgentFactory.create(),
                    events = TestEventFactory.createList(),
                    selectedTab = DetailTab.EVENTS,
                )
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = {},
                    )
                }
            }
            onNodeWithText("STATUS_CHANGE").assertIsDisplayed()
        }

    @Test
    fun `should show loading overlay when loading`() =
        runComposeUiTest {
            val state = AgentDetailUiState(isLoading = true)
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = {},
                    )
                }
            }
            onNodeWithText("Overview").assertIsDisplayed()
        }

    @Test
    fun `should show error banner when error is present`() =
        runComposeUiTest {
            val state =
                AgentDetailUiState(
                    agent = TestAgentFactory.create(),
                    error = "Something went wrong",
                )
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = {},
                    )
                }
            }
            onNodeWithText("Something went wrong").assertIsDisplayed()
        }

    @Test
    fun `should invoke onBackClick when back button is clicked`() =
        runComposeUiTest {
            var backClicked = false
            val state = AgentDetailUiState(agent = TestAgentFactory.create())
            setContent {
                DashboardTheme {
                    AgentDetailScreen(
                        uiState = state,
                        onTabSelected = {},
                        onBackClick = { backClicked = true },
                    )
                }
            }
            onNodeWithContentDescription("Back").performClick()
            assertEquals(true, backClicked)
        }
}
