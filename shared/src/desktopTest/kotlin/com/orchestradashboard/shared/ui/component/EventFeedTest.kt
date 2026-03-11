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
class EventFeedTest {
    @Test
    fun `should display all events`() =
        runComposeUiTest {
            val events = TestEventFactory.createList()
            setContent {
                DashboardTheme {
                    EventFeed(events = events)
                }
            }
            onNodeWithText("STATUS_CHANGE").assertIsDisplayed()
            onNodeWithText("PIPELINE_STARTED").assertIsDisplayed()
            onNodeWithText("ERROR").assertIsDisplayed()
        }

    @Test
    fun `should show empty state when no events`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    EventFeed(events = emptyList())
                }
            }
            onNodeWithText("No events yet.").assertIsDisplayed()
        }

    @Test
    fun `should display events in reverse chronological order`() =
        runComposeUiTest {
            val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val events =
                listOf(
                    TestEventFactory.create(id = "old", type = EventType.HEARTBEAT, timestamp = now - 60_000L),
                    TestEventFactory.create(id = "new", type = EventType.ERROR, timestamp = now),
                )
            setContent {
                DashboardTheme {
                    EventFeed(events = events)
                }
            }
            onNodeWithText("ERROR").assertIsDisplayed()
            onNodeWithText("HEARTBEAT").assertIsDisplayed()
        }
}
