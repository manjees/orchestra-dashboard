package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.model.SolveResponse
import com.orchestradashboard.shared.domain.repository.SolveRepository

class ExecuteSolveUseCase(
    private val repository: SolveRepository,
) {
    suspend operator fun invoke(request: SolveRequest): Result<SolveResponse> = repository.executeSolve(request)
}
