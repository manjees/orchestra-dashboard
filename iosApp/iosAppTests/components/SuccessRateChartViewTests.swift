import XCTest
import SwiftUI
@testable import iosApp

final class SuccessRateChartViewTests: XCTestCase {

    // MARK: - successRateLabel
    func testSuccessRateLabel_zeroRuns_returnsNoRuns() {
        XCTAssertEqual(SuccessRateChartView.successRateLabel(rate: 0.0, totalRuns: 0), "No runs")
    }
    func testSuccessRateLabel_75percent_returns75() {
        XCTAssertEqual(SuccessRateChartView.successRateLabel(rate: 0.75, totalRuns: 100), "75%")
    }
    func testSuccessRateLabel_100percent_returns100() {
        XCTAssertEqual(SuccessRateChartView.successRateLabel(rate: 1.0, totalRuns: 5), "100%")
    }
    func testSuccessRateLabel_clamps_aboveOne() {
        XCTAssertEqual(SuccessRateChartView.successRateLabel(rate: 1.5, totalRuns: 10), "100%")
    }
    func testSuccessRateLabel_clamps_belowZero() {
        XCTAssertEqual(SuccessRateChartView.successRateLabel(rate: -0.5, totalRuns: 10), "0%")
    }

    // MARK: - donutAngles
    func testDonutAngles_fullSuccess_successSweep360() {
        let angles = SuccessRateChartView.donutAngles(rate: 1.0)
        XCTAssertEqual(angles.successSweep, 360.0, accuracy: 0.001)
    }
    func testDonutAngles_halfSuccess_equalSweeps() {
        let angles = SuccessRateChartView.donutAngles(rate: 0.5)
        XCTAssertEqual(angles.successSweep, 180.0, accuracy: 0.001)
        XCTAssertEqual(angles.failureSweep, 180.0, accuracy: 0.001)
    }
    func testDonutAngles_zeroSuccess_failureSweep360() {
        let angles = SuccessRateChartView.donutAngles(rate: 0.0)
        XCTAssertEqual(angles.failureSweep, 360.0, accuracy: 0.001)
    }
    func testDonutAngles_startsAtNeg90() {
        let angles = SuccessRateChartView.donutAngles(rate: 0.75)
        // failureStart = -90 + successSweep(-90 + 270) = 180
        XCTAssertEqual(angles.failureStart, 180.0, accuracy: 0.001)
    }
}
