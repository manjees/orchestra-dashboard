package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.repository.SystemEventData
import com.orchestradashboard.shared.domain.repository.SystemRepository
import kotlinx.coroutines.flow.Flow

class ObserveSystemEventsUseCase(private val repository: SystemRepository) {
    operator fun invoke(): Flow<SystemEventData> = repository.observeSystemEvents()
}
