package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.HistoryFilter
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.repository.HistoryRepository

class GetPagedHistoryUseCase(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(
        filter: HistoryFilter = HistoryFilter(),
        page: Int = 0,
        pageSize: Int = DEFAULT_PAGE_SIZE,
    ): Result<PagedResult<PipelineResult>> = repository.getPagedHistory(filter, page, pageSize)

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
