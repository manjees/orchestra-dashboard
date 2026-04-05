import Foundation
import Shared

/// iOS-specific wrapper around the KMP PipelineMonitorViewModel.
/// Bridges Kotlin coroutine StateFlow to Swift's ObservableObject/Combine.
@MainActor
final class IOSPipelineMonitorViewModel: ObservableObject {
    @Published var pipeline: MonitoredPipeline? = nil
    @Published var logLines: [String] = []
    @Published var pendingApproval: ApprovalRequest? = nil
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    @Published var connectionStatus: ConnectionStatus = .disconnected

    // Parallel view state
    @Published var parallelPipelines: [MonitoredPipeline] = []
    @Published var parallelGroup: ParallelPipelineGroup? = nil
    @Published var isParallelView: Bool = false
    @Published var dependencies: [PipelineDependency] = []

    private let viewModel: PipelineMonitorViewModel
    private var collectTask: Task<Void, Never>?

    init(pipelineId: String) {
        let container = IOSAppContainer.shared
        self.viewModel = container.createPipelineMonitorViewModel(pipelineId: pipelineId)
        startCollecting()
    }

    private func startCollecting() {
        collectTask?.cancel()
        collectTask = Task { @MainActor [weak self] in
            guard let self else { return }
            let collector = PipelineUiStateCollector { [weak self] state in
                self?.updateFromState(state)
            }
            try? await self.viewModel.uiState.collect(collector: collector)
        }
    }

    private func updateFromState(_ state: PipelineMonitorUiState) {
        self.pipeline = state.pipeline
        self.logLines = state.logLines as? [String] ?? []
        self.pendingApproval = state.pendingApproval
        self.isLoading = state.isLoading
        self.error = state.error
        self.connectionStatus = state.connectionStatus
        // Parallel view state
        self.parallelPipelines = state.parallelPipelines as? [MonitoredPipeline] ?? []
        self.parallelGroup = state.parallelGroup
        self.isParallelView = state.isParallelView
        self.dependencies = state.dependencies as? [PipelineDependency] ?? []
    }

    func loadPipeline() {
        viewModel.loadPipeline()
    }

    func startObserving() {
        viewModel.startObserving()
    }

    func refresh() {
        viewModel.refresh()
    }

    func dismissApproval() {
        viewModel.dismissApproval()
    }

    func clearError() {
        viewModel.clearError()
    }

    func onCleared() {
        collectTask?.cancel()
        viewModel.onCleared()
    }
}

private final class PipelineUiStateCollector: Kotlinx_coroutines_coreFlowCollector {
    let onValue: (PipelineMonitorUiState) -> Void

    init(onValue: @escaping (PipelineMonitorUiState) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let state = value as? PipelineMonitorUiState {
            onValue(state)
        }
        completionHandler(nil)
    }
}
