package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.LogEntryDto
import com.orchestradashboard.shared.domain.model.LogEntry
import com.orchestradashboard.shared.domain.model.LogLevel

class LogEntryMapper {
    fun toDomain(
        dto: LogEntryDto,
        fallbackStepId: String,
    ): LogEntry =
        LogEntry(
            timestamp = dto.timestamp ?: "",
            level = parseLevel(dto.level),
            message = dto.message,
            stepId = dto.stepId ?: fallbackStepId,
        )

    private fun parseLevel(raw: String?): LogLevel =
        when (raw?.uppercase()) {
            "WARN", "WARNING" -> LogLevel.WARN
            "ERROR" -> LogLevel.ERROR
            "DEBUG" -> LogLevel.DEBUG
            else -> LogLevel.INFO
        }
}
