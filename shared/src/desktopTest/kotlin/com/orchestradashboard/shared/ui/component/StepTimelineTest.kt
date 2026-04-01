package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.MonitoredStep
import com.orchestradashboard.shared.domain.model.StepStatus
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StepTimelineTest {
    @Test
    fun `empty steps shows fallback text`() =
        runComposeUiTest {
            setContent { DashboardTheme { StepTimeline(steps = emptyList()) } }
            onNodeWithText("No steps available").assertIsDisplayed()
        }

    @Test
    fun `all nine step names are rendered`() =
        runComposeUiTest {
            val steps =
                NINE_STEP_NAMES.map { name ->
                    MonitoredStep(name, StepStatus.PENDING, 0L, null, "")
                }
            setContent { DashboardTheme { StepTimeline(steps = steps) } }
            NINE_STEP_NAMES.forEach { onNodeWithText(it).assertIsDisplayed() }
        }

    @Test
    fun `passed step displays elapsed time`() =
        runComposeUiTest {
            val steps =
                listOf(
                    MonitoredStep("build", StepStatus.PASSED, elapsedMs = 61_000L, null, ""),
                )
            setContent { DashboardTheme { StepTimeline(steps = steps) } }
            onNodeWithText("1m 1s").assertIsDisplayed()
        }

    private companion object {
        val NINE_STEP_NAMES =
            listOf(
                "plan", "design", "code", "review",
                "test", "build", "deploy", "verify", "done",
            )
    }
}
