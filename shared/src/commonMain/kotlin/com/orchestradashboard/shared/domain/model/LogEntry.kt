package com.orchestradashboard.shared.domain.model

data class LogEntry(
    val timestamp: String,
    val level: LogLevel,
    val message: String,
    val stepId: String,
)

enum class LogLevel { INFO, WARN, ERROR, DEBUG }
