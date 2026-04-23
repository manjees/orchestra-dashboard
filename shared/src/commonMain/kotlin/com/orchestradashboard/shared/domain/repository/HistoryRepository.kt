package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.HistoryDetail
import com.orchestradashboard.shared.domain.model.HistoryFilter
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.PipelineResult

interface HistoryRepository {
    suspend fun getPagedHistory(
        filter: HistoryFilter,
        page: Int,
        pageSize: Int,
    ): Result<PagedResult<PipelineResult>>

    suspend fun getHistoryDetail(id: String): Result<HistoryDetail>
}
