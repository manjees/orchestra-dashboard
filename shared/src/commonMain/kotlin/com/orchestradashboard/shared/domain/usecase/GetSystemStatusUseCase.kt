package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.SystemStatus
import com.orchestradashboard.shared.domain.repository.SystemRepository

class GetSystemStatusUseCase(private val repository: SystemRepository) {
    suspend operator fun invoke(): Result<SystemStatus> = repository.getSystemStatus()
}
