import Foundation
import Shared

@MainActor
final class IOSAnalyticsViewModel: ObservableObject {
    @Published var summary: PipelineAnalytics? = nil
    @Published var durationTrends: [DurationTrend] = []
    @Published var stepFailures: [StepFailureRate] = []
    @Published var selectedPeriod: PeriodFilter = .all
    @Published var isLoading: Bool = false
    @Published var error: String? = nil

    private let viewModel: AnalyticsViewModel
    private var collectTask: Task<Void, Never>?

    init(project: String) {
        self.viewModel = IOSAppContainer.shared.createAnalyticsViewModel(project: project)
        startCollecting()
        viewModel.loadData()
    }

    var hasData: Bool { summary != nil || !durationTrends.isEmpty }

    private func startCollecting() {
        collectTask?.cancel()
        collectTask = Task { @MainActor [weak self] in
            guard let self else { return }
            let collector = AnalyticsUiStateCollector { [weak self] state in
                self?.updateFromState(state)
            }
            try? await self.viewModel.uiState.collect(collector: collector)
        }
    }

    private func updateFromState(_ state: AnalyticsUiState) {
        self.summary = state.summary
        guard let trends = state.durationTrends as? [DurationTrend] else {
            fatalError("Failed to cast state.durationTrends. KMP-iOS bridge contract is broken.")
        }
        self.durationTrends = trends
        guard let failures = state.stepFailures as? [StepFailureRate] else {
            fatalError("Failed to cast state.stepFailures. KMP-iOS bridge contract is broken.")
        }
        self.stepFailures = failures
        self.selectedPeriod = state.selectedPeriod
        self.isLoading = state.isLoading
        self.error = state.error
    }

    func selectPeriod(_ period: PeriodFilter) {
        viewModel.selectPeriod(period: period)
    }

    func refresh() { viewModel.refresh() }
    func clearError() { viewModel.clearError() }

    func onCleared() {
        collectTask?.cancel()
        viewModel.onCleared()
    }
}

private final class AnalyticsUiStateCollector: Kotlinx_coroutines_coreFlowCollector {
    let onValue: (AnalyticsUiState) -> Void

    init(onValue: @escaping (AnalyticsUiState) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let state = value as? AnalyticsUiState {
            onValue(state)
        }
        completionHandler(nil)
    }
}
