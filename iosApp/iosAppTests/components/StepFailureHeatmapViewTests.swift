import XCTest
import SwiftUI
@testable import iosApp

final class StepFailureHeatmapViewTests: XCTestCase {

    // MARK: - failureBucketColor
    func testBucketColor_below25_returnsGreen() {
        let color = StepFailureHeatmapView.failureBucketColor(rate: 0.1)
        XCTAssertEqual(color, Color(red: 232.0/255.0, green: 245.0/255.0, blue: 233.0/255.0))
    }
    func testBucketColor_25to50_returnsLightGreen() {
        let color = StepFailureHeatmapView.failureBucketColor(rate: 0.4)
        XCTAssertEqual(color, Color(red: 200.0/255.0, green: 230.0/255.0, blue: 201.0/255.0))
    }
    func testBucketColor_50to75_returnsYellow() {
        let color = StepFailureHeatmapView.failureBucketColor(rate: 0.6)
        XCTAssertEqual(color, Color(red: 255.0/255.0, green: 249.0/255.0, blue: 196.0/255.0))
    }
    func testBucketColor_75to100_returnsOrange() {
        let color = StepFailureHeatmapView.failureBucketColor(rate: 0.8)
        XCTAssertEqual(color, Color(red: 255.0/255.0, green: 204.0/255.0, blue: 128.0/255.0))
    }
    func testBucketColor_100_returnsRed() {
        let color = StepFailureHeatmapView.failureBucketColor(rate: 1.0)
        XCTAssertEqual(color, Color(red: 239.0/255.0, green: 154.0/255.0, blue: 154.0/255.0))
    }

    // MARK: - failurePercentLabel
    func testPercentLabel_formatsCorrectly() {
        let label = StepFailureHeatmapView.failurePercentLabel(failed: 3, total: 10, rate: 0.3)
        XCTAssertEqual(label, "3/10 (30%)")
    }
}
