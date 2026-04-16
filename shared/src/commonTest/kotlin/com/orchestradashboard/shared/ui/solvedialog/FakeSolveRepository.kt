package com.orchestradashboard.shared.ui.solvedialog

import com.orchestradashboard.shared.domain.model.SolveRequest
import com.orchestradashboard.shared.domain.model.SolveResponse
import com.orchestradashboard.shared.domain.repository.SolveRepository

class FakeSolveRepository : SolveRepository {
    var result: Result<SolveResponse> = Result.success(SolveResponse("pipe-noop", "started"))

    var executeSolveCallCount = 0
        private set
    var lastRequest: SolveRequest? = null
        private set

    override suspend fun executeSolve(request: SolveRequest): Result<SolveResponse> {
        executeSolveCallCount++
        lastRequest = request
        return result
    }
}
