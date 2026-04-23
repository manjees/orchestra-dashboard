import SwiftUI
import Shared

struct SuccessRateChartView: View {
    let summary: PipelineAnalytics?

    var body: some View {
        GroupBox {
            if let summary {
                HStack(alignment: .center, spacing: 16) {
                    DonutCanvas(successRate: summary.successRate)
                        .frame(width: 80, height: 80)

                    VStack(alignment: .leading, spacing: 4) {
                        Text(SuccessRateChartView.successRateLabel(
                            rate: summary.successRate,
                            totalRuns: summary.totalRuns
                        ))
                        .font(.title2).bold()
                        Text("Total: \(summary.totalRuns)")
                            .font(.caption).foregroundColor(.secondary)
                        Text("Failed: \(summary.failedRuns)")
                            .font(.caption).foregroundColor(.red)
                        Text("Avg: \(String(format: "%.1f", summary.avgDurationSec))s")
                            .font(.caption).foregroundColor(.secondary)
                    }
                    Spacer()
                }
            } else {
                Text("No data available")
                    .font(.caption).foregroundColor(.secondary)
                    .frame(maxWidth: .infinity)
            }
        } label: {
            Text("Success Rate").font(.headline)
        }
    }

    static func successRateLabel(rate: Double, totalRuns: Int32) -> String {
        guard totalRuns > 0 else { return "No runs" }
        let clamped = min(1.0, max(0.0, rate))
        return "\(Int(clamped * 100))%"
    }

    static func donutAngles(rate: Double) -> (successSweep: Double, failureStart: Double, failureSweep: Double) {
        let clamped = min(1.0, max(0.0, rate))
        let successSweep = clamped * 360.0
        let failureStart = -90.0 + successSweep
        let failureSweep = (1.0 - clamped) * 360.0
        return (successSweep: successSweep, failureStart: failureStart, failureSweep: failureSweep)
    }
}

private struct DonutCanvas: View {
    let successRate: Double

    var body: some View {
        Canvas { context, size in
            let angles = SuccessRateChartView.donutAngles(rate: successRate)
            let center = CGPoint(x: size.width / 2, y: size.height / 2)
            let radius = min(size.width, size.height) / 2
            let lineWidth: CGFloat = 12

            func arc(startDeg: Double, sweepDeg: Double) -> Path {
                Path { path in
                    path.addArc(
                        center: center,
                        radius: radius - lineWidth / 2,
                        startAngle: .degrees(startDeg),
                        endAngle: .degrees(startDeg + sweepDeg),
                        clockwise: false
                    )
                }
            }

            if angles.successSweep > 0 {
                context.stroke(
                    arc(startDeg: -90, sweepDeg: angles.successSweep),
                    with: .color(.green),
                    lineWidth: lineWidth
                )
            }
            if angles.failureSweep > 0 {
                context.stroke(
                    arc(startDeg: angles.failureStart, sweepDeg: angles.failureSweep),
                    with: .color(.red),
                    lineWidth: lineWidth
                )
            }
        }
    }
}
