package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.AppSettings
import com.orchestradashboard.shared.domain.repository.SettingsRepository

class GetSettingsUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(): AppSettings =
        AppSettings(
            baseUrl = repository.getBaseUrl(),
            apiKey = repository.getApiKey(),
        )
}
