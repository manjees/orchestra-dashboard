import Foundation
import Shared

/// iOS-specific wrapper around the KMP ApprovalModalViewModel.
/// Bridges Kotlin coroutine StateFlow to Swift's ObservableObject/Combine.
///
/// The KMP `ApprovalModalViewModel` owns the canonical approval state
/// (pending request, countdown, timeout, submission, error). This class
/// surfaces those fields as `@Published` properties for SwiftUI.
@MainActor
final class IOSApprovalModalViewModel: ObservableObject {
    @Published var showDialog: Bool = false
    @Published var pendingApproval: ApprovalRequest? = nil
    @Published var remainingTimeSec: Int32? = nil
    @Published var isTimedOut: Bool = false
    @Published var isSubmitting: Bool = false
    @Published var approvalError: String? = nil

    private let viewModel: ApprovalModalViewModel
    private var collectTask: Task<Void, Never>?

    init(viewModel: ApprovalModalViewModel? = nil) {
        self.viewModel = viewModel ?? IOSAppContainer.shared.createApprovalModalViewModel()
        startCollecting()
    }

    private func startCollecting() {
        collectTask?.cancel()
        collectTask = Task { @MainActor [weak self] in
            guard let self else { return }
            let collector = ApprovalModalStateCollector { [weak self] state in
                self?.updateFromState(state)
            }
            try? await self.viewModel.uiState.collect(collector: collector)
        }
    }

    private func updateFromState(_ state: ApprovalModalState) {
        self.showDialog = state.showDialog
        self.pendingApproval = state.pendingApproval
        if let remaining = state.remainingTimeSec {
            self.remainingTimeSec = remaining.int32Value
        } else {
            self.remainingTimeSec = nil
        }
        self.isTimedOut = state.isTimedOut
        self.isSubmitting = state.isSubmitting
        self.approvalError = state.error
    }

    /// Submits a user decision. Maps the `ApprovalDecisionValue` enum (exposed
    /// to SwiftUI callers) to the matching KMP `ApprovalDecision` sealed-interface
    /// variant — `StrategyDecision`, `SupremeCourtDecision`, or `GenericDecision`.
    func respond(decision: ApprovalDecisionValue, comment: String = "") {
        let kmpDecision: ApprovalDecision = mapDecision(decision)
        viewModel.respond(decision: kmpDecision, comment: comment)
    }

    func dismiss() {
        viewModel.dismiss()
    }

    func clearError() {
        viewModel.clearError()
    }

    func onCleared() {
        collectTask?.cancel()
        viewModel.onCleared()
    }

    // MARK: - Decision Mapping

    private func mapDecision(_ decision: ApprovalDecisionValue) -> ApprovalDecision {
        switch decision {
        case .splitExecute:
            return StrategyDecision.splitExecute
        case .noSplit:
            return StrategyDecision.noSplit
        case .cancel:
            return StrategyDecision.cancel
        case .uphold:
            return SupremeCourtDecision.uphold
        case .overturn:
            return SupremeCourtDecision.overturn
        case .redesign:
            return SupremeCourtDecision.redesign
        case .approve:
            return GenericDecision(value: "approve")
        case .reject:
            return GenericDecision(value: "reject")
        default:
            return GenericDecision(value: decision.value)
        }
    }
}

private final class ApprovalModalStateCollector: Kotlinx_coroutines_coreFlowCollector {
    let onValue: (ApprovalModalState) -> Void

    init(onValue: @escaping (ApprovalModalState) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let state = value as? ApprovalModalState {
            onValue(state)
        }
        completionHandler(nil)
    }
}
