import SwiftUI
import Shared

/// Row view for a completed pipeline result.
struct PipelineResultRowView: View {
    let result: PipelineResult

    private var isPassed: Bool {
        result.status == .passed
    }

    var body: some View {
        HStack {
            Image(systemName: isPassed ? "checkmark.circle.fill" : "xmark.circle.fill")
                .foregroundColor(isPassed ? .green : .red)
            Text(result.projectName).font(.body)
            Text("#\(result.issueNum)")
                .font(.caption).foregroundStyle(.secondary)
            Spacer()
            Text(formatElapsed(result.elapsedTotalSec))
                .font(.caption).foregroundStyle(.secondary)
        }
        .padding(.vertical, 4)
    }

    private func formatElapsed(_ seconds: Double) -> String {
        let m = Int(seconds) / 60
        let s = Int(seconds) % 60
        return "\(m)m \(s)s"
    }
}
