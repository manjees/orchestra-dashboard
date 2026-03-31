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

    private let viewModel: ProjectExplorerViewModel

    init() {
        let container = IOSAppContainer.shared
        self.viewModel = container.createProjectExplorerViewModel()
    }

    func loadCheckpoints() {
        isLoading = true
        viewModel.loadCheckpoints()
    }

    func retryCheckpoint(checkpointId: String) {
        retryingCheckpointId = checkpointId
        viewModel.retryCheckpoint(checkpointId: checkpointId)
    }

    func clearError() {
        error = nil
        viewModel.clearError()
    }

    func clearRetryResult() {
        viewModel.clearRetryResult()
    }

    func onCleared() {
        viewModel.onCleared()
    }
}
