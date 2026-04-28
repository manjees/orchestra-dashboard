package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.LogEntry
import kotlinx.coroutines.flow.Flow

interface LogStreamRepository {
    fun observeLogStream(stepId: String): Flow<LogEntry>
}
