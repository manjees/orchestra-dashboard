import XCTest
import SwiftUI
@testable import iosApp

final class DurationTrendsChartViewTests: XCTestCase {

    // MARK: - axisLabels
    func testAxisLabels_emptyTrends_returnsNil() {
        XCTAssertNil(DurationTrendsChartView.axisLabels(from: []))
    }
    func testAxisLabels_singlePoint_firstEqualsLast() {
        let trend = DurationTrend(date: "2024-01-15", avgDurationSec: 120.0, runCount: 10)
        let labels = DurationTrendsChartView.axisLabels(from: [trend])
        XCTAssertNotNil(labels)
        XCTAssertEqual(labels?.first, labels?.last)
    }
    func testAxisLabels_multiplePoints_takesLast5Chars() {
        let trends = [
            DurationTrend(date: "2024-01-01", avgDurationSec: 100.0, runCount: 5),
            DurationTrend(date: "2024-01-15", avgDurationSec: 120.0, runCount: 8),
            DurationTrend(date: "2024-01-31", avgDurationSec: 110.0, runCount: 6),
        ]
        let labels = DurationTrendsChartView.axisLabels(from: trends)
        XCTAssertEqual(labels?.first, "01-01")
        XCTAssertEqual(labels?.last, "01-31")
    }
    func testAxisLabels_threeOrMore_includesMidLabel() {
        let trends = [
            DurationTrend(date: "2024-01-01", avgDurationSec: 100.0, runCount: 5),
            DurationTrend(date: "2024-01-15", avgDurationSec: 120.0, runCount: 8),
            DurationTrend(date: "2024-01-31", avgDurationSec: 110.0, runCount: 6),
        ]
        let labels = DurationTrendsChartView.axisLabels(from: trends)
        XCTAssertNotNil(labels?.mid)
        XCTAssertEqual(labels?.mid, "01-15")
    }
    func testAxisLabels_twoPoints_noMidLabel() {
        let trends = [
            DurationTrend(date: "2024-01-01", avgDurationSec: 100.0, runCount: 5),
            DurationTrend(date: "2024-01-31", avgDurationSec: 110.0, runCount: 6),
        ]
        let labels = DurationTrendsChartView.axisLabels(from: trends)
        XCTAssertNil(labels?.mid)
    }

    // MARK: - yNormalization
    func testNormalizeY_singleValue_returns0_5() {
        XCTAssertEqual(normalizeY(50.0, min: 50.0, max: 50.0), 0.5)
    }
    func testNormalizeY_minMaxRange_normalizes0to1() {
        XCTAssertEqual(normalizeY(0.0, min: 0.0, max: 100.0), 0.0, accuracy: 0.001)
        XCTAssertEqual(normalizeY(100.0, min: 0.0, max: 100.0), 1.0, accuracy: 0.001)
        XCTAssertEqual(normalizeY(50.0, min: 0.0, max: 100.0), 0.5, accuracy: 0.001)
    }

    // MARK: - Private helpers
    private func normalizeY(_ value: Double, min: Double, max: Double) -> Double {
        guard max != min else { return 0.5 }
        return (value - min) / (max - min)
    }
}
