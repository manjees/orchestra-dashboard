import Foundation
import Shared

/// iOS-specific wrapper around the KMP DashboardViewModel.
/// Bridges Kotlin coroutine Flows to Swift's ObservableObject/Combine.
@MainActor
final class IOSDashboardViewModel: ObservableObject {
    @Published var agents: [Agent] = []
    @Published var isLoading: Bool = false
    @Published var error: String? = nil

    private let viewModel: DashboardViewModel

    init() {
        let container = IOSAppContainer.shared
        self.viewModel = container.createDashboardViewModel()
    }

    func startObserving() {
        isLoading = true
        // TODO: Collect KMP StateFlow via Kotlin coroutines Swift interop
        // Use KMP-NativeCoroutines or manual collector pattern
        viewModel.startObserving()
        // UIState observation requires KMP-NativeCoroutines or SKIE integration
        // See: https://github.com/rickclephas/KMP-NativeCoroutines
    }

    func clearError() {
        error = nil
        viewModel.clearError()
    }

    func onCleared() {
        viewModel.onCleared()
    }
}
