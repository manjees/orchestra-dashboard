package com.orchestradashboard.shared.domain.model

enum class CommandType {
    START,
    STOP,
    RESTART,
}

enum class CommandStatus {
    PENDING,
    EXECUTING,
    COMPLETED,
    FAILED,
}

data class AgentCommand(
    val id: String,
    val agentId: String,
    val commandType: CommandType,
    val status: CommandStatus,
    val requestedAt: Long,
    val requestedBy: String,
    val executedAt: Long? = null,
    val completedAt: Long? = null,
    val failureReason: String? = null,
) {
    companion object {
        const val TIMEOUT_MS = 30_000L
        const val PENDING_TIMEOUT_MS = 60_000L
    }
}
