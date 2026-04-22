import SwiftUI
import Shared

/// Coordinator view that binds `IOSApprovalModalViewModel` to the stateless
/// `ApprovalDialogView` component and presents it as a modal sheet.
///
/// Use this from any screen that needs to react to pipeline approval requests
/// — it listens to the KMP `ApprovalModalViewModel.uiState` (via the iOS
/// wrapper) and drives sheet presentation, button enablement, timeout, and
/// error alerts automatically.
struct ApprovalModalView: View {
    @ObservedObject var viewModel: IOSApprovalModalViewModel

    var body: some View {
        EmptyView()
            .sheet(isPresented: Binding(
                get: { viewModel.showDialog },
                set: { newValue in
                    if !newValue { viewModel.dismiss() }
                }
            )) {
                if let approval = viewModel.pendingApproval {
                    ApprovalDialogView(
                        approval: approval,
                        remainingTimeSec: viewModel.remainingTimeSec,
                        isTimedOut: viewModel.isTimedOut,
                        isSubmitting: viewModel.isSubmitting,
                        error: viewModel.approvalError,
                        onRespond: { decision in viewModel.respond(decision: decision) },
                        onDismiss: { viewModel.dismiss() },
                        onClearError: { viewModel.clearError() }
                    )
                }
            }
    }
}

// MARK: - Previews

#Preview("Strategy — active") {
    struct PreviewHost: View {
        @StateObject var vm: IOSApprovalModalViewModel

        init() {
            let approvalVM = IOSApprovalModalViewModel()
            approvalVM.showDialog = true
            approvalVM.pendingApproval = ApprovalRequest(
                approvalType: "strategy",
                options: ["split_execute", "no_split", "cancel"],
                id: "preview-strategy-1",
                context: ApprovalContext(
                    eta: "~18 min",
                    splitProposal: "Split into 3 checkpoints",
                    detail: "Issue spans multiple modules"
                ),
                timeoutSec: 300,
                requestedAtMs: 0
            )
            approvalVM.remainingTimeSec = 240
            approvalVM.isTimedOut = false
            approvalVM.isSubmitting = false
            _vm = StateObject(wrappedValue: approvalVM)
        }

        var body: some View {
            ZStack {
                Text("Parent View Content")
                ApprovalModalView(viewModel: vm)
            }
        }
    }
    return PreviewHost()
}

#Preview("Supreme Court — active") {
    struct PreviewHost: View {
        @StateObject var vm: IOSApprovalModalViewModel

        init() {
            let approvalVM = IOSApprovalModalViewModel()
            approvalVM.showDialog = true
            approvalVM.pendingApproval = ApprovalRequest(
                approvalType: "supreme_court",
                options: ["uphold", "overturn", "redesign"],
                id: "preview-sc-1",
                context: ApprovalContext(
                    eta: nil,
                    splitProposal: nil,
                    detail: "Design review after 2 failed checkpoints"
                ),
                timeoutSec: 600,
                requestedAtMs: 0
            )
            approvalVM.remainingTimeSec = 480
            approvalVM.isTimedOut = false
            approvalVM.isSubmitting = false
            _vm = StateObject(wrappedValue: approvalVM)
        }

        var body: some View {
            ZStack {
                Text("Parent View Content")
                ApprovalModalView(viewModel: vm)
            }
        }
    }
    return PreviewHost()
}

#Preview("Timed out") {
    struct PreviewHost: View {
        @StateObject var vm: IOSApprovalModalViewModel

        init() {
            let approvalVM = IOSApprovalModalViewModel()
            approvalVM.showDialog = true
            approvalVM.pendingApproval = ApprovalRequest(
                approvalType: "strategy",
                options: ["split_execute", "no_split", "cancel"],
                id: "preview-timeout",
                context: nil,
                timeoutSec: 300,
                requestedAtMs: 0
            )
            approvalVM.remainingTimeSec = 0
            approvalVM.isTimedOut = true
            approvalVM.isSubmitting = false
            _vm = StateObject(wrappedValue: approvalVM)
        }

        var body: some View {
            ZStack {
                Text("Parent View Content")
                ApprovalModalView(viewModel: vm)
            }
        }
    }
    return PreviewHost()
}

#Preview("Submitting") {
    struct PreviewHost: View {
        @StateObject var vm: IOSApprovalModalViewModel

        init() {
            let approvalVM = IOSApprovalModalViewModel()
            approvalVM.showDialog = true
            approvalVM.pendingApproval = ApprovalRequest(
                approvalType: "strategy",
                options: ["split_execute", "no_split", "cancel"],
                id: "preview-submitting",
                context: nil,
                timeoutSec: 300,
                requestedAtMs: 0
            )
            approvalVM.remainingTimeSec = 200
            approvalVM.isTimedOut = false
            approvalVM.isSubmitting = true
            _vm = StateObject(wrappedValue: approvalVM)
        }

        var body: some View {
            ZStack {
                Text("Parent View Content")
                ApprovalModalView(viewModel: vm)
            }
        }
    }
    return PreviewHost()
}
