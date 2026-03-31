import SwiftUI
import Shared

/// Card view for a running pipeline.
struct ActivePipelineCardView: View {
    let pipeline: ActivePipeline

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(pipeline.projectName).font(.headline)
            Text("#\(pipeline.issueNum) \(pipeline.issueTitle)")
                .font(.subheadline)
                .lineLimit(1)
            HStack {
                if !pipeline.currentStep.isEmpty {
                    Label(pipeline.currentStep, systemImage: "gearshape")
                        .font(.caption)
                }
                Spacer()
                Text(formatElapsed(pipeline.elapsedTotalSec))
                    .font(.caption).foregroundStyle(.secondary)
            }
        }
        .padding()
        .background(RoundedRectangle(cornerRadius: 12).fill(.background))
        .shadow(radius: 2)
    }

    private func formatElapsed(_ seconds: Double) -> String {
        let m = Int(seconds) / 60
        let s = Int(seconds) % 60
        return "\(m)m \(s)s"
    }
}
