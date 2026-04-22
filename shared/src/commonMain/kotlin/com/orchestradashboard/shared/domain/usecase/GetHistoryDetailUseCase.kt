package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.HistoryDetail
import com.orchestradashboard.shared.domain.repository.HistoryRepository

class GetHistoryDetailUseCase(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(id: String): Result<HistoryDetail> = repository.getHistoryDetail(id)
}
