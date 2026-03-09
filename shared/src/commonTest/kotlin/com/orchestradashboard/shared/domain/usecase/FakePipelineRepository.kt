package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePipelineRepository(
    private val pipelineRuns: List<PipelineRun> = emptyList(),
) : PipelineRepository {
    override fun observePipelineRuns(agentId: String): Flow<List<PipelineRun>> = flowOf(pipelineRuns.filter { it.agentId == agentId })

    override suspend fun getPipelineRun(runId: String): Result<PipelineRun> =
        pipelineRuns.find { it.id == runId }
            ?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("PipelineRun $runId not found"))

    override fun observeActivePipelines(): Flow<List<PipelineRun>> =
        flowOf(
            pipelineRuns.filter {
                it.status == PipelineRunStatus.RUNNING || it.status == PipelineRunStatus.QUEUED
            },
        )
}
