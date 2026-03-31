import SwiftUI
import Shared

/// Horizontal step timeline displaying pipeline steps with status colors.
struct StepTimelineView: View {
    let steps: [MonitoredStep]

    var body: some View {
        if steps.isEmpty {
            Text("No steps available")
                .font(.caption)
                .foregroundStyle(.secondary)
        } else {
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
    }

    private func stepNode(_ step: MonitoredStep) -> some View {
        VStack(spacing: 4) {
            StepCircleView(status: step.status)

            Text(step.name)
                .font(.caption2)
                .lineLimit(1)
                .frame(width: 60)

            if step.status == .running, let startedAt = step.startedAtMs {
                RunningTimerText(startedAtMs: startedAt.int64Value)
            } else if step.elapsedMs > 0 {
                Text(StepTimelineView.formatElapsed(step.elapsedMs))
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
        }
    }

    static func statusColor(for status: StepStatus) -> Color {
        switch status {
        case .pending: return .gray
        case .running: return .blue
        case .passed: return .green
        case .failed: return .red
        case .skipped: return .orange
        default: return .gray
        }
    }

    static func connectorColor(for status: StepStatus) -> Color {
        switch status {
        case .passed: return .green
        case .running: return .blue
        default: return .gray
        }
    }

    static func formatElapsed(_ ms: Int64) -> String {
        let totalSec = Int(ms / 1000)
        let m = totalSec / 60
        let s = totalSec % 60
        return "\(m)m \(s)s"
    }
}

struct StepCircleView: View {
    let status: StepStatus
    @State private var isPulsing: Bool = false

    var body: some View {
        Circle()
            .fill(StepTimelineView.statusColor(for: status))
            .frame(width: 24, height: 24)
            .opacity(status == .running ? (isPulsing ? 1.0 : 0.4) : 1.0)
            .animation(
                status == .running
                    ? .easeInOut(duration: 0.8).repeatForever(autoreverses: true)
                    : .default,
                value: isPulsing
            )
            .onAppear {
                if status == .running { isPulsing = true }
            }
    }
}

struct RunningTimerText: View {
    let startedAtMs: Int64
    @State private var elapsed: Int64 = 0

    var body: some View {
        Text(StepTimelineView.formatElapsed(elapsed))
            .font(.caption2)
            .foregroundStyle(.blue)
            .onAppear {
                elapsed = Int64(Date().timeIntervalSince1970 * 1000) - startedAtMs
            }
            .task {
                while !Task.isCancelled {
                    try? await Task.sleep(nanoseconds: 1_000_000_000)
                    elapsed = Int64(Date().timeIntervalSince1970 * 1000) - startedAtMs
                }
            }
    }
}
