package com.orchestradashboard.shared.ui.history

import com.orchestradashboard.shared.domain.model.HistoryFilter
import com.orchestradashboard.shared.domain.model.TimeRange
import com.orchestradashboard.shared.domain.usecase.GetHistoryDetailUseCase
import com.orchestradashboard.shared.domain.usecase.GetPagedHistoryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 300L

class HistoryViewModel(
    private val getPagedHistoryUseCase: GetPagedHistoryUseCase,
    private val getHistoryDetailUseCase: GetHistoryDetailUseCase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var loadJob: Job? = null
    private var detailJob: Job? = null

    fun loadInitialData() {
        loadPage(page = 0, append = false)
    }

    fun applyFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(filter = filter) }
        loadPage(page = 0, append = false)
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                val nextFilter =
                    _uiState.value.filter.copy(
                        keyword = query.takeIf { it.isNotEmpty() },
                    )
                _uiState.update { it.copy(filter = nextFilter) }
                loadPage(page = 0, append = false)
            }
    }

    fun selectTimeRange(timeRange: TimeRange?) {
        val nextFilter = _uiState.value.filter.copy(timeRange = timeRange)
        applyFilter(nextFilter)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.hasNextPage || state.isLoading || state.isLoadingMore) return
        loadPage(page = state.currentPage + 1, append = true)
    }

    fun selectHistory(id: String) {
        detailJob?.cancel()
        _uiState.update {
            it.copy(
                selectedHistoryId = id,
                historyDetail = null,
                detailError = null,
                isLoadingDetail = true,
            )
        }
        detailJob =
            viewModelScope.launch {
                getHistoryDetailUseCase(id)
                    .onSuccess { detail ->
                        _uiState.update { it.copy(historyDetail = detail, isLoadingDetail = false) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(detailError = e.message, isLoadingDetail = false) }
                    }
            }
    }

    fun clearSelection() {
        detailJob?.cancel()
        _uiState.update {
            it.copy(
                selectedHistoryId = null,
                historyDetail = null,
                detailError = null,
                isLoadingDetail = false,
            )
        }
    }

    fun refresh() {
        loadPage(page = 0, append = false)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        searchJob?.cancel()
        loadJob?.cancel()
        detailJob?.cancel()
        viewModelScope.cancel()
    }

    private fun loadPage(
        page: Int,
        append: Boolean,
    ) {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                if (append) {
                    _uiState.update { it.copy(isLoadingMore = true) }
                } else {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                val filter = _uiState.value.filter
                getPagedHistoryUseCase(filter, page)
                    .onSuccess { result ->
                        _uiState.update { state ->
                            val items =
                                if (append) state.historyItems + result.agents else result.agents
                            state.copy(
                                historyItems = items,
                                currentPage = result.page,
                                totalPages = result.totalPages,
                                hasNextPage = result.hasNextPage,
                                isLoading = false,
                                isLoadingMore = false,
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = e.message,
                            )
                        }
                    }
            }
    }
}
