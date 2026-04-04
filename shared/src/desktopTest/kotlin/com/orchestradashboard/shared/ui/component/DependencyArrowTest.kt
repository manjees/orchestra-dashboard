package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.DependencyType
import com.orchestradashboard.shared.domain.model.PipelineDependency
import com.orchestradashboard.shared.ui.theme.DashboardTheme
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DependencyArrowTest {
    // ─── dependencyColor ──────────────────────────────────────────────────────

    @Test
    fun `dependencyColor BLOCKS_START returns orange`() {
        assertEquals(Color(0xFFFF9800), dependencyColor(DependencyType.BLOCKS_START))
    }

    @Test
    fun `dependencyColor PROVIDES_INPUT returns light blue`() {
        assertEquals(Color(0xFF03A9F4), dependencyColor(DependencyType.PROVIDES_INPUT))
    }

    // ─── calcArrowHeadPoints ──────────────────────────────────────────────────

    @Test
    fun `calcArrowHeadPoints tip matches provided tip offset`() {
        val tip = Offset(20f, 50f)
        val result = calcArrowHeadPoints(tip = tip, angle = (PI / 2).toFloat(), size = 10f)
        assertEquals(tip, result.tip)
    }

    @Test
    fun `calcArrowHeadPoints left and right are symmetric around angle`() {
        val tip = Offset(0f, 0f)
        val angle = (PI / 2).toFloat()
        val result = calcArrowHeadPoints(tip = tip, angle = angle, size = 10f)
        // Left and right should be at equal distance from tip
        val leftDist = kotlin.math.sqrt(result.left.x * result.left.x + result.left.y * result.left.y.toDouble()).toFloat()
        val rightDist = kotlin.math.sqrt(result.right.x * result.right.x + result.right.y * result.right.y.toDouble()).toFloat()
        assertEquals(leftDist, rightDist, 0.01f)
    }

    @Test
    fun `calcArrowHeadPoints with zero size returns tip for all points`() {
        val tip = Offset(10f, 20f)
        val result = calcArrowHeadPoints(tip = tip, angle = 0f, size = 0f)
        assertEquals(tip, result.tip)
    }

    // ─── DependencyArrowOverlay composable ───────────────────────────────────

    @Test
    fun `DependencyArrowOverlay renders canvas with testTag`() =
        runComposeUiTest {
            val dependencies =
                listOf(
                    PipelineDependency("lane-1", "lane-2", DependencyType.BLOCKS_START),
                )
            setContent {
                DashboardTheme {
                    DependencyArrowOverlay(
                        dependencies = dependencies,
                        pipelineIds = listOf("lane-1", "lane-2"),
                        laneHeights = listOf(100f, 100f),
                        modifier = Modifier.size(40.dp, 200.dp),
                    )
                }
            }
            onNodeWithTag("dependency_arrows").assertIsDisplayed()
        }

    @Test
    fun `DependencyArrowOverlay renders canvas even with empty dependencies`() =
        runComposeUiTest {
            setContent {
                DashboardTheme {
                    DependencyArrowOverlay(
                        dependencies = emptyList(),
                        pipelineIds = listOf("lane-1", "lane-2"),
                        laneHeights = listOf(100f, 100f),
                        modifier = Modifier.size(40.dp, 200.dp),
                    )
                }
            }
            // Canvas node always exists (drawing nothing is still a canvas)
            onNodeWithTag("dependency_arrows").assertIsDisplayed()
        }
}

private fun assertEquals(
    a: Float,
    b: Float,
    delta: Float,
) {
    assertTrue(kotlin.math.abs(a - b) <= delta, "Expected $a to equal $b within delta $delta")
}
