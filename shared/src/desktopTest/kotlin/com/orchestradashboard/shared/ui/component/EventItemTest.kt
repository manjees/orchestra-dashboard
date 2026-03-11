package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.EventType
import com.orchestradashboard.shared.ui.TestEventFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EventItemTest {
    @Test
    fun `displays event type label`() =
        runComposeUiTest {
            val event = TestEventFactory.create(type = EventType.HEARTBEAT)
            setContent { DashboardTheme { EventItem(event = event) } }
            onNodeWithText("HB").assertIsDisplayed()
        }

    @Test
    fun `displays payload preview truncated to one line`() =
        runComposeUiTest {
            val event = TestEventFactory.create(payload = "some payload data")
            setContent { DashboardTheme { EventItem(event = event) } }
            onNodeWithText("some payload data").assertIsDisplayed()
        }

    @Test
    fun `displays correct label for status change event`() =
        runComposeUiTest {
            val event = TestEventFactory.create(type = EventType.STATUS_CHANGE)
            setContent { DashboardTheme { EventItem(event = event) } }
            onNodeWithText("STATUS").assertIsDisplayed()
        }

    @Test
    fun `displays correct label for error event`() =
        runComposeUiTest {
            val event = TestEventFactory.create(type = EventType.ERROR)
            setContent { DashboardTheme { EventItem(event = event) } }
            onNodeWithText("ERR").assertIsDisplayed()
        }

    @Test
    fun `displays correct label for pipeline started event`() =
        runComposeUiTest {
            val event = TestEventFactory.create(type = EventType.PIPELINE_STARTED)
            setContent { DashboardTheme { EventItem(event = event) } }
            onNodeWithText("START").assertIsDisplayed()
        }

    @Test
    fun `displays correct label for pipeline completed event`() =
        runComposeUiTest {
            val event = TestEventFactory.create(type = EventType.PIPELINE_COMPLETED)
            setContent { DashboardTheme { EventItem(event = event) } }
            onNodeWithText("DONE").assertIsDisplayed()
        }
}
