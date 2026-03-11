package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StatusFilterBarTest {

    @Test
    fun `should display All chip and all status chips`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                StatusFilterBar(
                    selectedStatus = null,
                    onStatusSelected = {},
                )
            }
        }
        onNodeWithText("All").assertIsDisplayed()
        onNodeWithText("Running").assertIsDisplayed()
        onNodeWithText("Idle").assertIsDisplayed()
        onNodeWithText("Error").assertIsDisplayed()
        onNodeWithText("Offline").assertIsDisplayed()
    }

    @Test
    fun `should mark All chip as selected when no filter is applied`() = runComposeUiTest {
        setContent {
            DashboardTheme {
                StatusFilterBar(
                    selectedStatus = null,
                    onStatusSelected = {},
                )
            }
        }
        onNodeWithText("All").assertIsSelected()
    }

    @Test
    fun `should invoke callback with status when chip is clicked`() = runComposeUiTest {
        var selectedStatus: Agent.AgentStatus? = null
        setContent {
            DashboardTheme {
                StatusFilterBar(
                    selectedStatus = null,
                    onStatusSelected = { selectedStatus = it },
                )
            }
        }
        onNodeWithText("Running").performClick()
        assertEquals(Agent.AgentStatus.RUNNING, selectedStatus)
    }

    @Test
    fun `should invoke callback with null when All chip is clicked`() = runComposeUiTest {
        var callbackInvoked = false
        var selectedStatus: Agent.AgentStatus? = Agent.AgentStatus.RUNNING
        setContent {
            DashboardTheme {
                StatusFilterBar(
                    selectedStatus = Agent.AgentStatus.RUNNING,
                    onStatusSelected = {
                        selectedStatus = it
                        callbackInvoked = true
                    },
                )
            }
        }
        onNodeWithText("All").performClick()
        assertEquals(true, callbackInvoked)
        assertEquals(null, selectedStatus)
    }
}
