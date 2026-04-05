import XCTest
import SwiftUI
@testable import iosApp

final class ParallelPipelineViewTests: XCTestCase {

    // MARK: - arrowColor

    func testArrowColor_blocksStart_isOrange() {
        XCTAssertEqual(DependencyArrowCanvas.arrowColor(for: .blocksStart), .orange)
    }

    func testArrowColor_providesInput_isLightBlue() {
        let expected = Color(red: 0.01, green: 0.66, blue: 0.96)
        XCTAssertEqual(DependencyArrowCanvas.arrowColor(for: .providesInput), expected)
    }

    func testArrowColor_blocksStart_differsFromProvidesInput() {
        let orange = DependencyArrowCanvas.arrowColor(for: .blocksStart)
        let blue = DependencyArrowCanvas.arrowColor(for: .providesInput)
        XCTAssertNotEqual(orange, blue)
    }

    // MARK: - calcArrowEndpoints

    func testCalcArrowEndpoints_firstLane_centerIsHalfLaneHeight() {
        let (startY, _) = DependencyArrowCanvas.calcArrowEndpoints(
            sourceIdx: 0,
            targetIdx: 1,
            laneHeight: 100
        )
        XCTAssertEqual(startY, 50, "lane 0 center should be at half the lane height")
    }

    func testCalcArrowEndpoints_secondLane_centerIsOneAndHalfLaneHeight() {
        let (_, endY) = DependencyArrowCanvas.calcArrowEndpoints(
            sourceIdx: 0,
            targetIdx: 1,
            laneHeight: 100
        )
        XCTAssertEqual(endY, 150, "lane 1 center should be at 1.5x the lane height")
    }

    func testCalcArrowEndpoints_sourceBelowTarget_startYGreaterThanEndY() {
        let (startY, endY) = DependencyArrowCanvas.calcArrowEndpoints(
            sourceIdx: 2,
            targetIdx: 0,
            laneHeight: 100
        )
        XCTAssertGreaterThan(startY, endY)
    }

    func testCalcArrowEndpoints_differentLaneHeights_scalesCorrectly() {
        let (startY, endY) = DependencyArrowCanvas.calcArrowEndpoints(
            sourceIdx: 1,
            targetIdx: 3,
            laneHeight: 80
        )
        // lane 1: 1*80 + 40 = 120; lane 3: 3*80 + 40 = 280
        XCTAssertEqual(startY, 120)
        XCTAssertEqual(endY, 280)
    }

    func testCalcArrowEndpoints_sameLane_startEqualsEnd() {
        let (startY, endY) = DependencyArrowCanvas.calcArrowEndpoints(
            sourceIdx: 2,
            targetIdx: 2,
            laneHeight: 60
        )
        XCTAssertEqual(startY, endY)
    }

    // MARK: - Group A: statusBadgeColor

    func testStatusBadgeColor_running_isBlue() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeColor(for: .running), .blue)
    }

    func testStatusBadgeColor_passed_isGreen() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeColor(for: .passed), .green)
    }

    func testStatusBadgeColor_failed_isRed() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeColor(for: .failed), .red)
    }

    func testStatusBadgeColor_cancelled_isOrange() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeColor(for: .cancelled), .orange)
    }

    func testStatusBadgeColor_queued_isGray() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeColor(for: .queued), .gray)
    }

    // MARK: - Group A: statusBadgeLabel

    func testStatusBadgeLabel_running_returnsRunning() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeLabel(for: .running), "Running")
    }

    func testStatusBadgeLabel_passed_returnsPassed() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeLabel(for: .passed), "Passed")
    }

    func testStatusBadgeLabel_failed_returnsFailed() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeLabel(for: .failed), "Failed")
    }

    func testStatusBadgeLabel_cancelled_returnsCancelled() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeLabel(for: .cancelled), "Cancelled")
    }

    func testStatusBadgeLabel_queued_returnsQueued() {
        XCTAssertEqual(ParallelPipelineView.statusBadgeLabel(for: .queued), "Queued")
    }

    // MARK: - Group B: dependencyLegendLabel

    func testDependencyLegendLabels_blocksStartReturnsExpectedText() {
        XCTAssertEqual(ParallelPipelineView.dependencyLegendLabel(for: .blocksStart), "Blocks Start")
    }

    func testDependencyLegendLabels_providesInputReturnsExpectedText() {
        XCTAssertEqual(ParallelPipelineView.dependencyLegendLabel(for: .providesInput), "Provides Input")
    }

    // MARK: - Group C: calcLaneCenterY

    func testCalcLaneCenterY_firstLane_returnsHalfHeight() {
        let heights: [CGFloat] = [100, 80, 60]
        let y = DependencyArrowCanvas.calcLaneCenterY(index: 0, laneHeights: heights, spacing: 12)
        XCTAssertEqual(y, 50, "lane 0 center = heights[0]/2 = 50")
    }

    func testCalcLaneCenterY_secondLane_accountsForSpacing() {
        let heights: [CGFloat] = [100, 80, 60]
        let y = DependencyArrowCanvas.calcLaneCenterY(index: 1, laneHeights: heights, spacing: 12)
        // heights[0] + spacing + heights[1]/2 = 100 + 12 + 40 = 152
        XCTAssertEqual(y, 152, "lane 1 center = 100 + 12 + 40 = 152")
    }

    func testCalcLaneCenterY_thirdLane_accumulatesHeightsAndSpacing() {
        let heights: [CGFloat] = [100, 80, 60]
        let y = DependencyArrowCanvas.calcLaneCenterY(index: 2, laneHeights: heights, spacing: 12)
        // (100 + 12) + (80 + 12) + 60/2 = 112 + 92 + 30 = 234
        XCTAssertEqual(y, 234, "lane 2 center = 112 + 92 + 30 = 234")
    }

    func testCalcLaneCenterY_variableHeights_correctMidpoint() {
        let heights: [CGFloat] = [40, 120]
        let y = DependencyArrowCanvas.calcLaneCenterY(index: 1, laneHeights: heights, spacing: 8)
        // 40 + 8 + 120/2 = 40 + 8 + 60 = 108
        XCTAssertEqual(y, 108)
    }

    func testCalcLaneCenterY_emptyHeights_returnsZero() {
        let y = DependencyArrowCanvas.calcLaneCenterY(index: 0, laneHeights: [], spacing: 12)
        XCTAssertEqual(y, 0)
    }

    func testCalcLaneCenterY_outOfBoundsIndex_returnsZero() {
        let heights: [CGFloat] = [100, 80, 60]

        // Test index equal to count
        let y1 = DependencyArrowCanvas.calcLaneCenterY(index: 3, laneHeights: heights, spacing: 12)
        XCTAssertEqual(y1, 0, "Index equal to count should be out of bounds and return 0")

        // Test negative index
        let y2 = DependencyArrowCanvas.calcLaneCenterY(index: -1, laneHeights: heights, spacing: 12)
        XCTAssertEqual(y2, 0, "Negative index should be out of bounds and return 0")
    }
}
