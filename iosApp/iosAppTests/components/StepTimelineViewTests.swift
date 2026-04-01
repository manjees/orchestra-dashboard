import XCTest
@testable import iosApp

final class StepTimelineViewTests: XCTestCase {

    // MARK: - formatElapsed

    func testFormatElapsed_zero() {
        XCTAssertEqual(StepTimelineView.formatElapsed(0), "0m 0s")
    }

    func testFormatElapsed_oneSecond() {
        XCTAssertEqual(StepTimelineView.formatElapsed(1_000), "0m 1s")
    }

    func testFormatElapsed_oneMinute() {
        XCTAssertEqual(StepTimelineView.formatElapsed(60_000), "1m 0s")
    }

    func testFormatElapsed_oneMinuteThirty() {
        XCTAssertEqual(StepTimelineView.formatElapsed(90_500), "1m 30s")
    }

    func testFormatElapsed_largeValue() {
        XCTAssertEqual(StepTimelineView.formatElapsed(3_661_000), "61m 1s")
    }

    // MARK: - statusColor

    func testStatusColor_pending() {
        XCTAssertEqual(StepTimelineView.statusColor(for: .pending), .gray)
    }

    func testStatusColor_running() {
        XCTAssertEqual(StepTimelineView.statusColor(for: .running), .blue)
    }

    func testStatusColor_passed() {
        XCTAssertEqual(StepTimelineView.statusColor(for: .passed), .green)
    }

    func testStatusColor_failed() {
        XCTAssertEqual(StepTimelineView.statusColor(for: .failed), .red)
    }

    func testStatusColor_skipped() {
        XCTAssertEqual(StepTimelineView.statusColor(for: .skipped), .orange)
    }

    // MARK: - connectorColor

    func testConnectorColor_passed() {
        XCTAssertEqual(StepTimelineView.connectorColor(for: .passed), .green)
    }

    func testConnectorColor_running() {
        XCTAssertEqual(StepTimelineView.connectorColor(for: .running), .blue)
    }

    func testConnectorColor_pending() {
        XCTAssertEqual(StepTimelineView.connectorColor(for: .pending), .gray)
    }
}
