import SwiftUI
import Shared

struct StepFailureHeatmapView: View {
    let failures: [StepFailureRate]

    private var sortedFailures: [StepFailureRate] {
        failures.sorted { $0.failureRate > $1.failureRate }
    }

    var body: some View {
        GroupBox {
            if failures.isEmpty {
                Text("No failure data")
                    .font(.caption).foregroundColor(.secondary)
                    .frame(maxWidth: .infinity)
            } else {
                VStack(spacing: 6) {
                    ForEach(sortedFailures, id: \.stepName) { failure in
                        HStack(spacing: 8) {
                            Text(failure.stepName)
                                .font(.caption)
                                .lineLimit(1)
                                .truncationMode(.tail)
                                .frame(minWidth: 80, alignment: .leading)
                            RoundedRectangle(cornerRadius: 4)
                                .fill(StepFailureHeatmapView.failureBucketColor(rate: failure.failureRate))
                                .frame(width: 20, height: 20)
                            Text(StepFailureHeatmapView.failurePercentLabel(
                                failed: failure.failedCount,
                                total: failure.totalCount,
                                rate: failure.failureRate
                            ))
                            .font(.caption)
                            .foregroundColor(.secondary)
                            Spacer()
                        }
                    }
                }
            }
        } label: {
            Text("Step Failure Rates").font(.headline)
        }
    }

    static func failureBucketColor(rate: Double) -> Color {
        if rate >= 1.0 {
            return Color(red: 239.0/255.0, green: 154.0/255.0, blue: 154.0/255.0)
        } else if rate >= 0.75 {
            return Color(red: 255.0/255.0, green: 204.0/255.0, blue: 128.0/255.0)
        } else if rate >= 0.50 {
            return Color(red: 255.0/255.0, green: 249.0/255.0, blue: 196.0/255.0)
        } else if rate >= 0.25 {
            return Color(red: 200.0/255.0, green: 230.0/255.0, blue: 201.0/255.0)
        } else {
            return Color(red: 232.0/255.0, green: 245.0/255.0, blue: 233.0/255.0)
        }
    }

    static func failurePercentLabel(failed: Int32, total: Int32, rate: Double) -> String {
        return "\(failed)/\(total) (\(Int(rate * 100))%)"
    }
}
