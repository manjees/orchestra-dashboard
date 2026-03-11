package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.EventType
import com.orchestradashboard.shared.ui.TestEventFactory
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EventItemTest {
    @Test
    fun `should display event type label`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    EventItem(event = TestEventFactory.create(type = EventType.HEARTBEAT))
                }
            }
            onNodeWithText("HEARTBEAT").assertIsDisplayed()
        }

    @Test
    fun `should display payload preview`() =
        runComposeUiTest {
            val payload = """{"status": "ok"}"""
            setContent {
                DashboardTheme {
                    EventItem(event = TestEventFactory.create(payload = payload))
                }
            }
            onNodeWithText(payload).assertIsDisplayed()
        }

    @Test
    fun `should display event type icon`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    EventItem(event = TestEventFactory.create(type = EventType.ERROR))
                }
            }
            onNodeWithContentDescription("ERROR").assertIsDisplayed()
        }

    @Test
    fun `should display relative timestamp`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    EventItem(
                        event =
                            TestEventFactory.create(
                                timestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - 5000L,
                            ),
                    )
                }
            }
            onNodeWithText("5s ago").assertIsDisplayed()
        }
}
