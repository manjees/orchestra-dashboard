package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.ui.TestEventFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EventFeedTest {
    @Test
    fun `displays empty state when no events`() =
        runComposeUiTest {
            setContent { DashboardTheme { EventFeed(events = emptyList()) } }
            onNodeWithText("No events recorded.").assertIsDisplayed()
        }

    @Test
    fun `displays events when list is not empty`() =
        runComposeUiTest {
            val events = TestEventFactory.createList()
            setContent { DashboardTheme { EventFeed(events = events) } }
            onNodeWithText("HB").assertIsDisplayed()
            onNodeWithText("STATUS").assertIsDisplayed()
            onNodeWithText("ERR").assertIsDisplayed()
        }

    @Test
    fun `each event shows type label`() =
        runComposeUiTest {
            val events = TestEventFactory.createList()
            setContent { DashboardTheme { EventFeed(events = events) } }
            onNodeWithText("HB").assertIsDisplayed()
            onNodeWithText("STATUS").assertIsDisplayed()
            onNodeWithText("ERR").assertIsDisplayed()
        }
}
