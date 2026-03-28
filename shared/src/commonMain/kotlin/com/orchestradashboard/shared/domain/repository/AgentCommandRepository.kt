package com.orchestradashboard.shared.domain.repository

import com.orchestradashboard.shared.domain.model.AgentCommand
import com.orchestradashboard.shared.domain.model.CommandType

interface AgentCommandRepository {
    suspend fun sendCommand(
        agentId: String,
        commandType: CommandType,
    ): Result<AgentCommand>

    suspend fun getCommands(
        agentId: String,
        limit: Int = 20,
    ): Result<List<AgentCommand>>
}
