package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.model.SolveResponse

interface SolveRepository {
    suspend fun executeSolve(request: SolveRequest): Result<SolveResponse>
}
