package com.orchestradashboard.shared.ui.history

import com.orchestradashboard.shared.domain.model.HistoryDetail
import com.orchestradashboard.shared.domain.model.HistoryFilter
import com.orchestradashboard.shared.domain.model.PipelineResult

data class HistoryUiState(
    val historyItems: List<PipelineResult> = emptyList(),
    val filter: HistoryFilter = HistoryFilter(),
    val searchQuery: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val selectedHistoryId: String? = null,
    val historyDetail: HistoryDetail? = null,
    val isLoadingDetail: Boolean = false,
    val detailError: String? = null,
) {
    val hasResults: Boolean get() = historyItems.isNotEmpty()
    val isFiltered: Boolean get() = filter != HistoryFilter() || searchQuery.isNotEmpty()
    val showDetail: Boolean get() = selectedHistoryId != null
}
