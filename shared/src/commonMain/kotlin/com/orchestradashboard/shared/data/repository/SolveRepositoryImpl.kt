package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.SolveCommandMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.model.SolveResponse
import com.orchestradashboard.shared.domain.repository.SolveRepository

class SolveRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: SolveCommandMapper,
) : SolveRepository {
    override suspend fun executeSolve(request: SolveRequest): Result<SolveResponse> =
        runCatching {
            val requestDto = mapper.toDto(request)
            val responseDto = api.postSolve(requestDto)
            mapper.toDomain(responseDto)
        }
}
