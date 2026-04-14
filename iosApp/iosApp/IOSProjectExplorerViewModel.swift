import Foundation
import Shared

/// iOS-specific wrapper around the KMP ProjectExplorerViewModel.
/// Bridges Kotlin coroutine Flows to Swift's ObservableObject/Combine.
@MainActor
final class IOSProjectExplorerViewModel: ObservableObject {
    @Published var checkpoints: [Checkpoint] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    @Published var retryingCheckpointId: String? = nil
    @Published var showSolveDialog: Bool = false
    @Published var selectedIssueIds: Set<Int> = []
    @Published var solveMode: SolveModeOption = .auto
    @Published var isParallel: Bool = false
    @Published var isSolving: Bool = false
    @Published var solveError: String? = nil
    @Published var solveResultPipelineId: String? = nil

    private let kmpViewModel: ProjectExplorerViewModel
    private var stateCollectionTask: Task<Void, Never>?

    init() {
        self.kmpViewModel = IOSAppContainer.shared.createProjectExplorerViewModel()
        startCollectingState()
    }

    private func startCollectingState() {
        stateCollectionTask = Task { @MainActor [weak self] in
            guard let self else { return }
            for await state in kmpViewModel.uiState {
                self.isLoading = state.isLoading
                self.error = state.error
                self.checkpoints = state.checkpoints
                self.retryingCheckpointId = state.retryingCheckpointId
                self.showSolveDialog = state.showSolveDialog
                self.selectedIssueIds = Set(state.selectedIssues.map { Int(truncating: $0) })
                self.solveMode = SolveModeOption.from(state.solveMode)
                self.isParallel = state.isParallel
                self.isSolving = state.isSolving
                self.solveError = state.solveError
                self.solveResultPipelineId = state.solveResult?.pipelineId
            }
        }
    }

    func loadInitialData() {
        kmpViewModel.loadInitialData()
    }

    func retryCheckpoint(checkpointId: String) {
        kmpViewModel.retryCheckpoint(checkpointId: checkpointId)
    }

    func clearError() {
        kmpViewModel.clearError()
    }

    func clearRetryResult() {
        kmpViewModel.clearRetryResult()
    }

    func openSolveDialog(issue: Issue) {
        kmpViewModel.openSolveDialog(issue: issue)
    }

    func toggleIssueSelection(issueNumber: Int) {
        kmpViewModel.toggleIssueSelection(issueNumber: Int32(issueNumber))
    }

    func setSolveMode(_ mode: SolveModeOption) {
        kmpViewModel.setSolveMode(mode: mode.toKmpSolveMode)
    }

    func toggleParallel() {
        kmpViewModel.toggleParallel()
    }

    func executeSolve() {
        kmpViewModel.executeSolve()
    }

    func closeSolveDialog() {
        kmpViewModel.closeSolveDialog()
    }

    func onCleared() {
        stateCollectionTask?.cancel()
        kmpViewModel.onCleared()
    }
}

private extension SolveModeOption {
    var toKmpSolveMode: SolveMode {
        switch self {
        case .express: return SolveMode.express
        case .standard: return SolveMode.standard
        case .full: return SolveMode.full
        case .auto: return SolveMode.auto
        }
    }

    static func from(_ kmpMode: SolveMode) -> SolveModeOption {
        switch kmpMode {
        case SolveMode.express: return .express
        case SolveMode.standard: return .standard
        case SolveMode.full: return .full
        default: return .auto
        }
    }
}
