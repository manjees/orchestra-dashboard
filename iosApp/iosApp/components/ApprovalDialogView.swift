import SwiftUI
import Shared

/// Modal dialog for pipeline approval requests.
/// Supports Strategy and Supreme Court approval types with timeout countdown.
struct ApprovalDialogView: View {
    let approval: ApprovalRequest
    let remainingTimeSec: Int32?
    let isTimedOut: Bool
    let isSubmitting: Bool
    let error: String?
    let onRespond: (ApprovalDecisionValue) -> Void
    let onDismiss: () -> Void
    let onClearError: () -> Void

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
                if isSubmitting {
                    ToolbarItem(placement: .navigationBarLeading) {
                        ProgressView()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Dismiss", action: onDismiss)
                }
            }
            .alert(
                "Error",
                isPresented: Binding(
                    get: { error != nil },
                    set: { _ in onClearError() }
                )
            ) {
                Button("OK") { onClearError() }
            } message: {
                Text(error ?? "")
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
            Button(action: { onRespond(.splitExecute) }) {
                Text("Split & Execute")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(isSubmitting)

            Button(action: { onRespond(.noSplit) }) {
                Text("No Split (Full)")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .disabled(isSubmitting)

            Button(role: .destructive, action: { onRespond(.cancel) }) {
                Text("Cancel")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .disabled(isSubmitting)
        }
    }

    private var supremeCourtButtons: some View {
        VStack(spacing: 8) {
            Button(action: { onRespond(.uphold) }) {
                Text("Uphold")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(isSubmitting)

            Button(action: { onRespond(.overturn) }) {
                Text("Overturn")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .disabled(isSubmitting)

            Button(action: { onRespond(.redesign) }) {
                Text("Redesign")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .disabled(isSubmitting)
        }
    }

    private var genericButtons: some View {
        HStack(spacing: 8) {
            Button(action: { onRespond(.approve) }) {
                Text("Approve")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(isSubmitting)

            Button(action: { onRespond(.reject) }) {
                Text("Reject")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .disabled(isSubmitting)
        }
    }

    // MARK: - Helpers

    private func formatTime(_ totalSeconds: Int) -> String {
        let minutes = totalSeconds / 60
        let seconds = totalSeconds % 60
        return "\(String(format: "%02d", minutes)):\(String(format: "%02d", seconds)) remaining"
    }
}
