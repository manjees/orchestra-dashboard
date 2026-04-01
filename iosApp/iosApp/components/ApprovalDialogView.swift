import SwiftUI
import Shared

/// Modal dialog for pipeline approval requests.
struct ApprovalDialogView: View {
    let approval: ApprovalRequest
    let onDismiss: () -> Void

    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                Text("Approval Required")
                    .font(.title2)
                    .bold()

                Text("This step requires \(approval.approvalType) approval.")
                    .font(.body)

                if !approval.options.isEmpty {
                    VStack(spacing: 8) {
                        Text("Options:")
                            .font(.headline)

                        ForEach(approval.options, id: \.self) { option in
                            Button(action: { /* Phase 2 */ }) {
                                Text(option)
                                    .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.bordered)
                        }

                        Text("Actions available in Phase 2")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
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
}
