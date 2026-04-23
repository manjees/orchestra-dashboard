import SwiftUI
import Charts
import Shared

struct DurationTrendsChartView: View {
    let trends: [DurationTrend]

    var body: some View {
        GroupBox {
            if trends.isEmpty {
                Text("No data available")
                    .font(.caption).foregroundColor(.secondary)
                    .frame(maxWidth: .infinity)
            } else {
                Chart(Array(trends.enumerated()), id: \.offset) { index, trend in
                    LineMark(
                        x: .value("Date", index),
                        y: .value("Duration (s)", trend.avgDurationSec)
                    )
                    .foregroundStyle(.blue)
                    PointMark(
                        x: .value("Date", index),
                        y: .value("Duration (s)", trend.avgDurationSec)
                    )
                    .foregroundStyle(.blue)
                }
                .chartXAxis {
                    if let labels = DurationTrendsChartView.axisLabels(from: trends) {
                        AxisMarks(values: [0, trends.count / 2, trends.count - 1]) { value in
                            AxisValueLabel {
                                if let idx = value.as(Int.self) {
                                    let label: String
                                    if idx == 0 { label = labels.first }
                                    else if idx == trends.count - 1 { label = labels.last }
                                    else { label = labels.mid ?? "" }
                                    Text(label).font(.caption2)
                                }
                            }
                        }
                    }
                }
                .chartYAxis {
                    AxisMarks { value in
                        AxisValueLabel {
                            if let v = value.as(Double.self) {
                                Text("\(Int(v))s").font(.caption2)
                            }
                        }
                    }
                }
                .frame(height: 160)
            }
        } label: {
            Text("Duration Trends").font(.headline)
        }
    }

    static func axisLabels(from trends: [DurationTrend]) -> (first: String, mid: String?, last: String)? {
        guard !trends.isEmpty else { return nil }
        func label(_ date: String) -> String { String(date.suffix(5)) }
        let first = label(trends[0].date)
        let last = label(trends[trends.count - 1].date)
        let mid: String? = trends.count >= 3 ? label(trends[trends.count / 2].date) : nil
        return (first: first, mid: mid, last: last)
    }
}
