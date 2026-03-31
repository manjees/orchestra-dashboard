import SwiftUI
import Shared

/// Horizontal step timeline displaying pipeline steps with status colors.
struct StepTimelineView: View {
    let steps: [MonitoredStep]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 4) {
                ForEach(Array(steps.enumerated()), id: \.offset) { index, step in
                    HStack(spacing: 4) {
                        stepNode(step)

                        if index < steps.count - 1 {
                            Rectangle()
                                .fill(connectorColor(for: step.status))
                                .frame(width: 24, height: 2)
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
        }
    }

    private func stepNode(_ step: MonitoredStep) -> some View {
        VStack(spacing: 4) {
            Circle()
                .fill(statusColor(for: step.status))
                .frame(width: 24, height: 24)

            Text(step.name)
                .font(.caption2)
                .lineLimit(1)
                .frame(width: 60)

            if step.elapsedMs > 0 {
                Text(formatElapsed(step.elapsedMs))
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private func statusColor(for status: StepStatus) -> Color {
        switch status {
        case .pending: return .gray
        case .running: return .blue
        case .passed: return .green
        case .failed: return .red
        case .skipped: return .orange
        default: return .gray
        }
    }

    private func connectorColor(for status: StepStatus) -> Color {
        switch status {
        case .passed: return .green
        case .running: return .blue
        default: return .gray
        }
    }

    private func formatElapsed(_ ms: Int64) -> String {
        let totalSec = Int(ms / 1000)
        let m = totalSec / 60
        let s = totalSec % 60
        return "\(m)m \(s)s"
    }
}
