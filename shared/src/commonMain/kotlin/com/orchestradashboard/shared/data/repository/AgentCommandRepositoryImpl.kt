package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.mapper.AgentCommandMapper
import com.orchestradashboard.shared.data.network.DashboardApi
import com.orchestradashboard.shared.domain.model.AgentCommand
import com.orchestradashboard.shared.domain.model.CommandType
import com.orchestradashboard.shared.domain.repository.AgentCommandRepository

class AgentCommandRepositoryImpl(
    private val api: DashboardApi,
    private val mapper: AgentCommandMapper,
) : AgentCommandRepository {
    override suspend fun sendCommand(
        agentId: String,
        commandType: CommandType,
    ): Result<AgentCommand> = runCatching { mapper.toDomain(api.sendCommand(agentId, commandType.name)) }

    override suspend fun getCommands(
        agentId: String,
        limit: Int,
    ): Result<List<AgentCommand>> = runCatching { api.getCommands(agentId, limit).map { mapper.toDomain(it) } }
}
