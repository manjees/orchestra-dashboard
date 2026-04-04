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
}
