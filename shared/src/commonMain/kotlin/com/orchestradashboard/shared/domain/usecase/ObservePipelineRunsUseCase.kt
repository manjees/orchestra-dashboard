package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import kotlinx.coroutines.flow.Flow

class ObservePipelineRunsUseCase(
    private val pipelineRepository: PipelineRepository,
) {
    operator fun invoke(agentId: String): Flow<List<PipelineRun>> = pipelineRepository.observePipelineRuns(agentId)
}
