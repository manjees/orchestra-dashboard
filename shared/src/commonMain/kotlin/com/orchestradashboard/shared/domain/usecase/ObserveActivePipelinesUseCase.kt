package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes all currently active pipeline runs.
 *
 * @param pipelineRepository Data source for pipeline information
 */
class ObserveActivePipelinesUseCase(
    private val pipelineRepository: PipelineRepository,
) {
    /**
     * Invokes the use case.
     *
     * @return [Flow] emitting updated lists of active pipeline runs
     */
    operator fun invoke(): Flow<List<PipelineRun>> = pipelineRepository.observeActivePipelines()
}
