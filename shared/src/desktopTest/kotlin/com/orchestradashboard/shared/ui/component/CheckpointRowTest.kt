package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.orchestradashboard.shared.domain.model.Checkpoint
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CheckpointRowTest {
    private fun testCheckpoint(
        step: String = "lint",
        status: CheckpointStatus = CheckpointStatus.FAILED,
        pipelineId: String = "pipe-42",
    ) = Checkpoint(
        id = "cp-1",
        pipelineId = pipelineId,
        createdAt = Instant.parse("2025-03-10T08:00:00Z"),
        step = step,
        status = status,
    )

    @Test
    fun `displays checkpoint step and status`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    CheckpointRow(
                        checkpoint = testCheckpoint(step = "test", status = CheckpointStatus.PASSED),
                        onRetryClick = {},
                    )
                }
            }
            onNodeWithText("test — passed").assertIsDisplayed()
        }

    @Test
    fun `displays pipeline ID`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    CheckpointRow(
                        checkpoint = testCheckpoint(pipelineId = "pipe-99"),
                        onRetryClick = {},
                    )
                }
            }
            onNodeWithText("Pipeline: pipe-99").assertIsDisplayed()
        }

    @Test
    fun `Retry button is displayed for failed checkpoints`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    CheckpointRow(
                        checkpoint = testCheckpoint(status = CheckpointStatus.FAILED),
                        onRetryClick = {},
                    )
                }
            }
            onNodeWithText("Retry").assertIsDisplayed()
        }

    @Test
    fun `Retry button onClick callback fires`() =
        runComposeUiTest {
            var clicked = false
            setContent {
                DashboardTheme {
                    CheckpointRow(
                        checkpoint = testCheckpoint(status = CheckpointStatus.FAILED),
                        onRetryClick = { clicked = true },
                    )
                }
            }
            onNodeWithText("Retry").performClick()
            assertTrue(clicked)
        }

    @Test
    fun `Retry button is not displayed for passed checkpoints`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    CheckpointRow(
                        checkpoint = testCheckpoint(status = CheckpointStatus.PASSED),
                        onRetryClick = {},
                    )
                }
            }
            onNodeWithText("Retry").assertDoesNotExist()
        }
}
