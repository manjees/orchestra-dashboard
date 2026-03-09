package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.PipelineRun
import com.orchestradashboard.shared.domain.repository.PipelineRepository

/**
 * Retrieves a single pipeline run by ID.
 *
 * @param pipelineRepository Data source for pipeline information
 */
class GetPipelineRunUseCase(
    private val pipelineRepository: PipelineRepository,
) {
    /**
     * Invokes the use case.
     *
     * @param runId Unique pipeline run identifier
     * @return [Result] containing the pipeline run on success, or an exception on failure
     */
    suspend operator fun invoke(runId: String): Result<PipelineRun> = pipelineRepository.getPipelineRun(runId)
}
