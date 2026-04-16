import Foundation
import Shared

/// iOS-specific wrapper around the KMP SolveDialogViewModel.
/// Bridges Kotlin coroutine StateFlow to Swift's ObservableObject/Combine.
@MainActor
final class IOSSolveDialogViewModel: ObservableObject {
    @Published var showDialog: Bool = false
    @Published var projectName: String? = nil
    @Published var selectedIssueIds: Set<Int> = []
    @Published var mode: SolveModeOption = .auto
    @Published var isParallel: Bool = false
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    @Published var resultPipelineId: String? = nil

    private let kmpViewModel: SolveDialogViewModel
    private var stateCollectionTask: Task<Void, Never>?

    init() {
        self.kmpViewModel = IOSAppContainer.shared.createSolveDialogViewModel()
        startCollectingState()
    }

    private func startCollectingState() {
        stateCollectionTask = Task { @MainActor [weak self] in
            guard let self else { return }
            for await state in kmpViewModel.uiState {
                self.showDialog = state.showDialog
                self.projectName = state.projectName
                self.selectedIssueIds = Set(state.selectedIssues.map { Int(truncating: $0) })
                self.mode = SolveModeOption.from(state.mode)
                self.isParallel = state.isParallel
                self.isLoading = state.isLoading
                self.error = state.error
                self.resultPipelineId = state.result?.pipelineId
            }
        }
    }

    func open(project: Project, issue: Issue) {
        kmpViewModel.open(project: project, issue: issue)
    }

    func toggleIssueSelection(issueNumber: Int) {
        kmpViewModel.toggleIssueSelection(issueNumber: Int32(issueNumber))
    }

    func setMode(_ newMode: SolveModeOption) {
        kmpViewModel.setMode(mode: newMode.toKmpSolveMode)
    }

    func toggleParallel() {
        kmpViewModel.toggleParallel()
    }

    func executeSolve() {
        kmpViewModel.executeSolve()
    }

    func clearError() {
        kmpViewModel.clearError()
    }

    func consumeResult() {
        kmpViewModel.consumeResult()
    }

    func close() {
        kmpViewModel.close()
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
