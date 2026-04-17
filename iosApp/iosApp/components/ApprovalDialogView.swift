import SwiftUI
import Shared

/// Modal dialog for pipeline approval requests.
/// Supports Strategy and Supreme Court approval types with timeout countdown.
struct ApprovalDialogView: View {
    let approval: ApprovalRequest
    let remainingTimeSec: Int32?
    let isTimedOut: Bool
    let onRespond: (String) -> Void
    let onDismiss: () -> Void

    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                Text(titleText)
                    .font(.title2)
                    .bold()

                Text("This step requires \(approval.approvalType) approval.")
                    .font(.body)

                // Context information
                contextSection

                // Countdown
                countdownSection

                // Action buttons or auto-approved message
                if isTimedOut {
                    Text("Auto-approved: timeout reached. The server has automatically approved this request.")
                        .font(.body)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.top, 8)
                } else {
                    actionButtons
                }

                Spacer()
            }
            .padding()
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Dismiss", action: onDismiss)
                }
            }
        }
    }

    // MARK: - Title

    private var titleText: String {
        switch approval.approvalType {
        case "strategy":
            return "Strategy Approval Required"
        case "supreme_court":
            return "Supreme Court Review Required"
        default:
            return "Approval Required: \(approval.approvalType)"
        }
    }

    // MARK: - Context Section

    @ViewBuilder
    private var contextSection: some View {
        if let context = approval.context {
            VStack(alignment: .leading, spacing: 4) {
                if let eta = context.eta {
                    Text("ETA: \(eta)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                if let proposal = context.splitProposal {
                    Text("Split Proposal: \(proposal)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                if let detail = context.detail {
                    Text(detail)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    // MARK: - Countdown Section

    @ViewBuilder
    private var countdownSection: some View {
        if let remaining = remainingTimeSec {
            VStack(spacing: 4) {
                let progress = approval.timeoutSec > 0
                    ? Float(remaining) / Float(approval.timeoutSec)
                    : 0.0
                ProgressView(value: Double(progress))
                    .tint(isTimedOut ? .red : .blue)

                HStack {
                    Spacer()
                    Text(isTimedOut ? "Timed out" : formatTime(Int(remaining)))
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
        }
    }

    // MARK: - Action Buttons

    @ViewBuilder
    private var actionButtons: some View {
        switch approval.approvalType {
        case "strategy":
            strategyButtons
        case "supreme_court":
            supremeCourtButtons
        default:
            genericButtons
        }
    }

    private var strategyButtons: some View {
        VStack(spacing: 8) {
            Button(action: { onRespond("split_execute") }) {
                Text("Split & Execute")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)

            Button(action: { onRespond("no_split") }) {
                Text("No Split (Full)")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)

            Button(role: .destructive, action: { onRespond("cancel") }) {
                Text("Cancel")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
        }
    }

    private var supremeCourtButtons: some View {
        VStack(spacing: 8) {
            Button(action: { onRespond("uphold") }) {
                Text("Uphold")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)

            Button(action: { onRespond("overturn") }) {
                Text("Overturn")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)

            Button(action: { onRespond("redesign") }) {
                Text("Redesign")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
        }
    }

    private var genericButtons: some View {
        HStack(spacing: 8) {
            Button(action: { onRespond("approve") }) {
                Text("Approve")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)

            Button(action: { onRespond("reject") }) {
                Text("Reject")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
        }
    }

    // MARK: - Helpers

    private func formatTime(_ totalSeconds: Int) -> String {
        let minutes = totalSeconds / 60
        let seconds = totalSeconds % 60
        return "\(minutes):\(String(format: "%02d", seconds)) remaining"
    }
}
