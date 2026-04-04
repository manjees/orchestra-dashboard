package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.MonitoredStep
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class StepNodeTest {
    @Test
    fun `formatElapsedMs 0ms returns 0m 0s`() = assertEquals("0m 0s", formatElapsedMs(0L))

    @Test
    fun `formatElapsedMs 1000ms returns 0m 1s`() = assertEquals("0m 1s", formatElapsedMs(1_000L))

    @Test
    fun `formatElapsedMs 60000ms returns 1m 0s`() = assertEquals("1m 0s", formatElapsedMs(60_000L))

    @Test
    fun `formatElapsedMs 90500ms returns 1m 30s`() = assertEquals("1m 30s", formatElapsedMs(90_500L))

    @Test
    fun `formatElapsedMs large value minutes grow unbounded`() = assertEquals("61m 1s", formatElapsedMs(3_661_000L))

    @Test
    fun `stepStatusColor PENDING is gray`() = assertEquals(Color(0xFF9E9E9E), stepStatusColor(StepStatus.PENDING))

    @Test
    fun `stepStatusColor RUNNING is blue`() = assertEquals(Color(0xFF2196F3), stepStatusColor(StepStatus.RUNNING))

    @Test
    fun `stepStatusColor PASSED is green`() = assertEquals(Color(0xFF4CAF50), stepStatusColor(StepStatus.PASSED))

    @Test
    fun `stepStatusColor FAILED is red`() = assertEquals(Color(0xFFF44336), stepStatusColor(StepStatus.FAILED))

    @Test
    fun `stepStatusColor SKIPPED is orange`() = assertEquals(Color(0xFFFF9800), stepStatusColor(StepStatus.SKIPPED))

    @Test
    fun `StepNode displays step name`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    StepNode(step = makeStep("planning", StepStatus.PENDING))
                }
            }
            onNodeWithText("planning").assertIsDisplayed()
        }

    @Test
    fun `StepNode shows formatted elapsed for PASSED step`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    StepNode(step = makeStep("coding", StepStatus.PASSED, elapsedMs = 90_500L))
                }
            }
            onNodeWithText("1m 30s").assertIsDisplayed()
        }

    @Test
    fun `StepNode PENDING with no elapsed shows no timer text`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    StepNode(step = makeStep("testing", StepStatus.PENDING, elapsedMs = 0L))
                }
            }
            onNodeWithText("testing").assertIsDisplayed()
        }

    @Test
    fun `StepNode RUNNING step with startedAtMs shows live timer`() =
        runComposeUiTest {
            mainClock.autoAdvance = false
            val startedAt = Clock.System.now().toEpochMilliseconds() - 90_000L
            setContent {
                DashboardTheme {
                    StepNode(step = makeStep("running-step", StepStatus.RUNNING, startedAtMs = startedAt))
                }
            }
            mainClock.advanceTimeByFrame()
            onNodeWithText("running-step").assertIsDisplayed()
            onNodeWithText("1m 30s").assertIsDisplayed()
        }

    @Test
    fun `StepNode RUNNING step without startedAtMs shows no timer`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    StepNode(step = makeStep("running-no-start", StepStatus.RUNNING, startedAtMs = null))
                }
            }
            onNodeWithText("running-no-start").assertIsDisplayed()
        }

    private fun makeStep(
        name: String,
        status: StepStatus,
        elapsedMs: Long = 0L,
        startedAtMs: Long? = null,
    ) = MonitoredStep(
        name = name,
        status = status,
        elapsedMs = elapsedMs,
        startedAtMs = startedAtMs,
        detail = "",
    )
}
