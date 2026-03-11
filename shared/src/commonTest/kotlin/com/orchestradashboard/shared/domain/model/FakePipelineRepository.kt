package com.orchestradashboard.shared.domain.model

import com.orchestradashboard.shared.domain.repository.PipelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class FakePipelineRepository : PipelineRepository {
    val pipelineRunsFlow = MutableSharedFlow<List<PipelineRun>>()

    var shouldFailObserve: Boolean = false
    var observeError: Throwable = RuntimeException("Pipeline connection failed")

    override fun observePipelineRuns(agentId: String): Flow<List<PipelineRun>> {
        if (shouldFailObserve) return flow { throw observeError }
        return pipelineRunsFlow
    }

    override suspend fun getPipelineRun(runId: String): Result<PipelineRun> = Result.failure(NotImplementedError())

    override fun observeActivePipelines(): Flow<List<PipelineRun>> = pipelineRunsFlow
}
