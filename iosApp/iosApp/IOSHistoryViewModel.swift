import Foundation
import Shared

/// iOS-specific wrapper around the KMP HistoryViewModel.
/// Bridges Kotlin coroutine StateFlow to Swift's ObservableObject/Combine.
@MainActor
final class IOSHistoryViewModel: ObservableObject {
    @Published var historyItems: [PipelineResult] = []
    @Published var isLoading: Bool = false
    @Published var isLoadingMore: Bool = false
    @Published var error: String? = nil
    @Published var searchQuery: String = ""
    @Published var hasNextPage: Bool = false
    @Published var showDetail: Bool = false
    @Published var historyDetail: HistoryDetail? = nil
    @Published var isLoadingDetail: Bool = false
    @Published var detailError: String? = nil
    @Published var selectedStatus: PipelineRunStatus? = nil
    @Published var selectedTimeRange: TimeRange? = nil

    private let viewModel: HistoryViewModel
    private var collectTask: Task<Void, Never>?

    init() {
        self.viewModel = IOSAppContainer.shared.createHistoryViewModel()
        startCollecting()
        viewModel.loadInitialData()
    }

    private func startCollecting() {
        collectTask?.cancel()
        collectTask = Task { @MainActor [weak self] in
            guard let self else { return }
            let collector = HistoryUiStateCollector { [weak self] state in
                self?.updateFromState(state)
            }
            try? await self.viewModel.uiState.collect(collector: collector)
        }
    }

    private func updateFromState(_ state: HistoryUiState) {
        self.historyItems = state.historyItems as? [PipelineResult] ?? []
        self.isLoading = state.isLoading
        self.isLoadingMore = state.isLoadingMore
        self.error = state.error
        self.searchQuery = state.searchQuery
        self.hasNextPage = state.hasNextPage
        self.showDetail = state.showDetail
        self.historyDetail = state.historyDetail
        self.isLoadingDetail = state.isLoadingDetail
        self.detailError = state.detailError
        self.selectedStatus = state.filter.status
        self.selectedTimeRange = state.filter.timeRange
    }

    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }

    func applyStatus(_ status: PipelineRunStatus?) {
        let current = viewModel.uiState.value.filter
        let next = HistoryFilter(
            project: current.project,
            status: status,
            keyword: current.keyword,
            timeRange: current.timeRange
        )
        viewModel.applyFilter(filter: next)
    }

    func selectTimeRange(_ range: TimeRange?) {
        viewModel.selectTimeRange(timeRange: range)
    }

    func loadNextPage() {
        viewModel.loadNextPage()
    }

    func selectHistory(_ id: String) {
        viewModel.selectHistory(id: id)
    }

    func clearSelection() {
        viewModel.clearSelection()
    }

    func clearError() {
        viewModel.clearError()
    }

    func refresh() {
        viewModel.refresh()
    }

    func onCleared() {
        collectTask?.cancel()
        viewModel.onCleared()
    }
}

private final class HistoryUiStateCollector: Kotlinx_coroutines_coreFlowCollector {
    let onValue: (HistoryUiState) -> Void

    init(onValue: @escaping (HistoryUiState) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let state = value as? HistoryUiState {
            onValue(state)
        }
        completionHandler(nil)
    }
}
