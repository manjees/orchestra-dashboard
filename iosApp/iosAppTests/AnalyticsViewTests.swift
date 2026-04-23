import XCTest
import SwiftUI
@testable import iosApp

final class AnalyticsViewTests: XCTestCase {

    // MARK: - periodFilterLabel
    func testPeriodFilterLabel_allCases() {
        XCTAssertEqual(AnalyticsView.periodLabel(.week), "Week")
        XCTAssertEqual(AnalyticsView.periodLabel(.month), "Month")
        XCTAssertEqual(AnalyticsView.periodLabel(.all), "All")
    }

    // MARK: - hasAnyData
    func testHasAnyData_allNilOrEmpty_returnsFalse() {
        XCTAssertFalse(AnalyticsView.hasAnyData(summary: nil, trends: [], failures: []))
    }
    func testHasAnyData_summaryPresent_returnsTrue() {
        let summary = PipelineAnalytics(project: "test", successRate: 0.75, avgDurationSec: 120.0, totalRuns: 100, failedRuns: 25)
        XCTAssertTrue(AnalyticsView.hasAnyData(summary: summary, trends: [], failures: []))
    }
    func testHasAnyData_trendsPresent_returnsTrue() {
        let trend = DurationTrend(date: "2024-01-15", avgDurationSec: 120.0, runCount: 10)
        XCTAssertTrue(AnalyticsView.hasAnyData(summary: nil, trends: [trend], failures: []))
    }
    func testHasAnyData_failuresPresent_returnsTrue() {
        let failure = StepFailureRate(stepName: "build", failedCount: 2, totalCount: 5, failureRate: 0.4)
        XCTAssertTrue(AnalyticsView.hasAnyData(summary: nil, trends: [], failures: [failure]))
    }
}
