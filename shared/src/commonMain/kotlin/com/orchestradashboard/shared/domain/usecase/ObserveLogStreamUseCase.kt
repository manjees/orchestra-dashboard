package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.LogEntry
import com.orchestradashboard.shared.domain.repository.LogStreamRepository
import kotlinx.coroutines.flow.Flow

class ObserveLogStreamUseCase(
    private val repository: LogStreamRepository,
) {
    operator fun invoke(stepId: String): Flow<LogEntry> = repository.observeLogStream(stepId)
}
