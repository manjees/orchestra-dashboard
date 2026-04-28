import Foundation
import Shared

/// iOS-specific wrapper around the KMP LogStreamViewModel.
/// Bridges Kotlin coroutine StateFlow to Swift's ObservableObject/Combine.
@MainActor
final class IOSLogStreamViewModel: ObservableObject {
    @Published var streamState: LogStreamState = LogStreamStateIdle()
    @Published var logs: [LogEntry] = []
    @Published var selectedStepId: String? = nil

    private let viewModel: LogStreamViewModel
    private var collectTask: Task<Void, Never>?

    init() {
        self.viewModel = IOSAppContainer.shared.createLogStreamViewModel()
        startCollecting()
    }

    func startStream(stepId: String) {
        viewModel.startStream(stepId: stepId)
    }

    func stopStream() {
        viewModel.stopStream()
    }

    func onCleared() {
        collectTask?.cancel()
        viewModel.onCleared()
    }

    private func startCollecting() {
        collectTask?.cancel()
        collectTask = Task { @MainActor [weak self] in
            guard let self else { return }
            let collector = LogStreamUiStateCollector { [weak self] state in
                self?.updateFromState(state)
            }
            try? await self.viewModel.uiState.collect(collector: collector)
        }
    }

    private func updateFromState(_ state: LogStreamUiState) {
        self.streamState = state.streamState
        self.logs = state.logs as? [LogEntry] ?? []
        self.selectedStepId = state.selectedStepId
    }
}

private final class LogStreamUiStateCollector: Kotlinx_coroutines_coreFlowCollector {
    let onValue: (LogStreamUiState) -> Void

    init(onValue: @escaping (LogStreamUiState) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let state = value as? LogStreamUiState {
            onValue(state)
        }
        completionHandler(nil)
    }
}
