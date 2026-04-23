package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.OrchestratorApi
import com.orchestradashboard.shared.data.mapper.LogEntryMapper
import com.orchestradashboard.shared.domain.model.LogEntry
import com.orchestradashboard.shared.domain.repository.LogStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LogStreamRepositoryImpl(
    private val orchestratorApi: OrchestratorApi,
    private val mapper: LogEntryMapper = LogEntryMapper(),
) : LogStreamRepository {
    override fun observeLogStream(stepId: String): Flow<LogEntry> =
        orchestratorApi.connectLogStream(stepId)
            .map { dto -> mapper.toDomain(dto, fallbackStepId = stepId) }
}
