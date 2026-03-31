import Foundation
import Shared

/// iOS-specific wrapper around the KMP DashboardHomeViewModel.
/// Bridges Kotlin coroutine StateFlow to Swift's ObservableObject/Combine.
@MainActor
final class IOSDashboardHomeViewModel: ObservableObject {
    @Published var systemStatus: SystemStatus? = nil
    @Published var activePipelines: [ActivePipeline] = []
    @Published var recentResults: [PipelineResult] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil
    @Published var connectionStatus: ConnectionStatus = .disconnected

    private let viewModel: DashboardHomeViewModel

    init() {
        let container = IOSAppContainer.shared
        self.viewModel = container.createDashboardHomeViewModel()
    }

    func loadInitialData() {
        isLoading = true
        viewModel.loadInitialData()
    }

    func startObserving() {
        viewModel.startObserving()
    }

    func refresh() {
        viewModel.refresh()
    }

    func clearError() {
        error = nil
        viewModel.clearError()
    }

    func onCleared() {
        viewModel.onCleared()
    }
}
