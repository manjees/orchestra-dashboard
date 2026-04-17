package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.StepFailureRate
import com.orchestradashboard.shared.domain.repository.AnalyticsRepository

/**
 * Retrieves step-level failure rates for a given project.
 */
class GetStepFailureRatesUseCase(
    private val analyticsRepository: AnalyticsRepository,
) {
    suspend operator fun invoke(project: String): Result<List<StepFailureRate>> = analyticsRepository.getStepFailureRates(project)
}
