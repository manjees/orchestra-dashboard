package com.orchestradashboard.shared.ui.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.orchestradashboard.shared.domain.model.DependencyType
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DependencyArrowGeometryTest {
    // ─── calcArrowEndpoints ────────────────────────────────────────────────────

    @Test
    fun calcArrowEndpoints_sameColumn_returnsVerticalLine() {
        val endpoints =
            calcArrowEndpoints(
                sourceLaneIndex = 0,
                targetLaneIndex = 1,
                laneHeight = 100f,
                laneStartY = 0f,
                laneWidth = 400f,
            )
        // Both x coordinates use arrowPadding default (8f)
        assertEquals(8f, endpoints.startX)
        assertEquals(8f, endpoints.endX)
        // startY = 0 + 0*100 + 50 = 50
        assertEquals(50f, endpoints.startY)
        // endY = 0 + 1*100 + 50 = 150
        assertEquals(150f, endpoints.endY)
    }

    @Test
    fun calcArrowEndpoints_differentColumns_returnsDiagonalPath() {
        val endpoints =
            calcArrowEndpoints(
                sourceLaneIndex = 0,
                targetLaneIndex = 2,
                laneHeight = 80f,
                laneStartY = 10f,
                laneWidth = 300f,
            )
        // startY = 10 + 0*80 + 40 = 50
        assertEquals(50f, endpoints.startY)
        // endY = 10 + 2*80 + 40 = 210
        assertEquals(210f, endpoints.endY)
    }

    @Test
    fun calcArrowEndpoints_sourceBelowTarget_returnsUpwardPath() {
        val endpoints =
            calcArrowEndpoints(
                sourceLaneIndex = 2,
                targetLaneIndex = 0,
                laneHeight = 100f,
                laneStartY = 0f,
                laneWidth = 400f,
            )
        // startY = 250, endY = 50
        assertTrue(endpoints.startY > endpoints.endY)
    }

    @Test
    fun calcArrowEndpoints_customPadding_usedForXCoordinates() {
        val endpoints =
            calcArrowEndpoints(
                sourceLaneIndex = 0,
                targetLaneIndex = 1,
                laneHeight = 100f,
                laneStartY = 0f,
                laneWidth = 400f,
                arrowPadding = 20f,
            )
        assertEquals(20f, endpoints.startX)
        assertEquals(20f, endpoints.endX)
    }

    @Test
    fun calcArrowEndpoints_withLaneStartY_offsetsYCorrectly() {
        val endpoints =
            calcArrowEndpoints(
                sourceLaneIndex = 0,
                targetLaneIndex = 1,
                laneHeight = 60f,
                laneStartY = 30f,
                laneWidth = 200f,
            )
        // startY = 30 + 0*60 + 30 = 60
        assertEquals(60f, endpoints.startY)
        // endY = 30 + 1*60 + 30 = 120
        assertEquals(120f, endpoints.endY)
    }

    @Test
    fun calcArrowEndpoints_sameLaneIndex_returnsEqualYValues() {
        val endpoints =
            calcArrowEndpoints(
                sourceLaneIndex = 1,
                targetLaneIndex = 1,
                laneHeight = 100f,
                laneStartY = 0f,
                laneWidth = 400f,
            )
        assertEquals(endpoints.startY, endpoints.endY)
    }

    // ─── calcLaneCenterY ──────────────────────────────────────────────────────

    @Test
    fun calcLaneCenterY_firstLane_returnsHalfHeight() {
        val result =
            calcLaneCenterY(
                laneIndex = 0,
                laneHeights = listOf(100f, 80f, 120f),
                laneSpacingPx = 12f,
            )
        // y = 0 (no preceding lanes) + 100/2 = 50
        assertEquals(50f, result)
    }

    @Test
    fun calcLaneCenterY_secondLane_includesFirstHeightAndSpacing() {
        val result =
            calcLaneCenterY(
                laneIndex = 1,
                laneHeights = listOf(100f, 80f),
                laneSpacingPx = 12f,
            )
        // y = 100 + 12 + 80/2 = 152
        assertEquals(152f, result)
    }

    @Test
    fun calcLaneCenterY_thirdLane_accumulatesPrecedingLanes() {
        val result =
            calcLaneCenterY(
                laneIndex = 2,
                laneHeights = listOf(100f, 80f, 60f),
                laneSpacingPx = 10f,
            )
        // y = (100+10) + (80+10) + 60/2 = 110 + 90 + 30 = 230
        assertEquals(230f, result)
    }

    @Test
    fun calcLaneCenterY_withLaneStartY_addsOffset() {
        val result =
            calcLaneCenterY(
                laneIndex = 0,
                laneHeights = listOf(100f),
                laneSpacingPx = 0f,
                laneStartY = 20f,
            )
        // y = 20 + 100/2 = 70
        assertEquals(70f, result)
    }

    @Test
    fun calcLaneCenterY_noSpacing_ignoresSpacing() {
        val result =
            calcLaneCenterY(
                laneIndex = 1,
                laneHeights = listOf(100f, 80f),
                laneSpacingPx = 0f,
            )
        // y = 100 + 0 + 80/2 = 140
        assertEquals(140f, result)
    }

    // ─── calcArrowHeadPoints ──────────────────────────────────────────────────

    @Test
    fun calcArrowHeadPoints_tipIsPreserved() {
        val tip = Offset(10f, 20f)
        val result = calcArrowHeadPoints(tip = tip, angle = (PI / 2).toFloat())
        assertEquals(tip, result.tip)
    }

    @Test
    fun calcArrowHeadPoints_leftAndRightAreDifferent() {
        val tip = Offset(0f, 0f)
        val result = calcArrowHeadPoints(tip = tip, angle = (PI / 2).toFloat())
        assertTrue(result.left != result.right)
    }

    @Test
    fun calcArrowHeadPoints_downwardArrow_basePointsAboveTip() {
        // angle = PI/2 → pointing down; base corners should be above the tip
        val tip = Offset(0f, 100f)
        val result = calcArrowHeadPoints(tip = tip, angle = (PI / 2).toFloat())
        assertTrue(result.left.y < tip.y)
        assertTrue(result.right.y < tip.y)
    }

    @Test
    fun calcArrowHeadPoints_customSize_scalesBaseDistance() {
        val tip = Offset(0f, 0f)
        val small = calcArrowHeadPoints(tip = tip, angle = (PI / 2).toFloat(), size = 5f)
        val large = calcArrowHeadPoints(tip = tip, angle = (PI / 2).toFloat(), size = 20f)
        // Larger size → base corners farther from tip
        val smallDist = small.left.y * small.left.y + small.left.x * small.left.x
        val largeDist = large.left.y * large.left.y + large.left.x * large.left.x
        assertTrue(largeDist > smallDist)
    }

    // ─── dependencyColor ──────────────────────────────────────────────────────

    @Test
    fun dependencyColor_blocksStart_returnsOrange() {
        val color = dependencyColor(DependencyType.BLOCKS_START)
        assertEquals(Color(0xFFFF9800.toInt()), color)
    }

    @Test
    fun dependencyColor_providesInput_returnsLightBlue() {
        val color = dependencyColor(DependencyType.PROVIDES_INPUT)
        assertEquals(Color(0xFF03A9F4.toInt()), color)
    }

    @Test
    fun dependencyColor_blocksStart_differsFromProvidesInput() {
        val orange = dependencyColor(DependencyType.BLOCKS_START)
        val blue = dependencyColor(DependencyType.PROVIDES_INPUT)
        assertTrue(orange != blue)
    }
}
