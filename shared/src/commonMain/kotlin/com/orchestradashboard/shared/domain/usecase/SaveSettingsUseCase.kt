package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.repository.SettingsRepository

class SaveSettingsUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(
        baseUrl: String,
        apiKey: String,
    ) {
        repository.saveBaseUrl(baseUrl)
        repository.saveApiKey(apiKey)
    }
}
