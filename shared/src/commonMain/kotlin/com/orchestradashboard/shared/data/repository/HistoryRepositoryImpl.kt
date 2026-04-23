package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.HistoryDetailMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.HistoryDetail
import com.orchestradashboard.shared.domain.model.HistoryFilter
import com.orchestradashboard.shared.domain.model.PagedResult
import com.orchestradashboard.shared.domain.model.PipelineResult
import com.orchestradashboard.shared.domain.repository.HistoryRepository

class HistoryRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: HistoryDetailMapper,
) : HistoryRepository {
    override suspend fun getPagedHistory(
        filter: HistoryFilter,
        page: Int,
        pageSize: Int,
    ): Result<PagedResult<PipelineResult>> =
        runCatching {
            val pageDto =
                api.getPagedHistory(
                    project = filter.project,
                    status = filter.status?.name,
                    keyword = filter.keyword,
                    hours = filter.timeRange?.hours,
                    page = page,
                    size = pageSize,
                )
            mapper.toPagedDomain(pageDto)
        }

    override suspend fun getHistoryDetail(id: String): Result<HistoryDetail> = runCatching { mapper.toDomain(api.getHistoryDetail(id)) }
}
