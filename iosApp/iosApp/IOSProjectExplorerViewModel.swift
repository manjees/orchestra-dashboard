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

    func onCleared() {
        stateCollectionTask?.cancel()
        kmpViewModel.onCleared()
    }
}
