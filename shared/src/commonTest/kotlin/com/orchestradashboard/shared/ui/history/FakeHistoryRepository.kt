package com.orchestradashboard.shared.ui.history

import com.orchestradashboard.shared.domain.model.HistoryDetail
import com.orchestradashboard.shared.domain.model.HistoryFilter
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.repository.HistoryRepository

class FakeHistoryRepository : HistoryRepository {
    var pagedHistoryResult: Result<PagedResult<PipelineResult>> =
        Result.success(PagedResult(emptyList(), 0, 20, 0L, 0))
    var historyDetailResult: Result<HistoryDetail> =
        Result.failure(RuntimeException("not found"))

    var lastFilter: HistoryFilter? = null
        private set
    var lastPage: Int = 0
        private set
    var lastPageSize: Int = 0
        private set
    var getPagedHistoryCallCount: Int = 0
        private set
    var getHistoryDetailCallCount: Int = 0
        private set
    var lastRequestedId: String? = null
        private set

    override suspend fun getPagedHistory(
        filter: HistoryFilter,
        page: Int,
        pageSize: Int,
    ): Result<PagedResult<PipelineResult>> {
        getPagedHistoryCallCount++
        lastFilter = filter
        lastPage = page
        lastPageSize = pageSize
        return pagedHistoryResult
    }

    override suspend fun getHistoryDetail(id: String): Result<HistoryDetail> {
        getHistoryDetailCallCount++
        lastRequestedId = id
        return historyDetailResult
    }
}
