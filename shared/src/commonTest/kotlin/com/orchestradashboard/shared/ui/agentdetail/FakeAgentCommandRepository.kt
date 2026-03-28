package com.orchestradashboard.shared.ui.agentdetail

import com.orchestradashboard.shared.domain.model.AgentCommand
import com.orchestradashboard.shared.domain.model.CommandStatus
import com.orchestradashboard.shared.domain.model.CommandType
import com.orchestradashboard.shared.domain.repository.AgentCommandRepository

class FakeAgentCommandRepository : AgentCommandRepository {
    var sendCommandResult: Result<AgentCommand> =
        Result.success(
            AgentCommand(
                id = "cmd-1",
                agentId = "agent-1",
                commandType = CommandType.STOP,
                status = CommandStatus.PENDING,
                requestedAt = 1000L,
                requestedBy = "test-user",
            ),
        )

    override suspend fun sendCommand(
        agentId: String,
        commandType: CommandType,
    ): Result<AgentCommand> = sendCommandResult

    override suspend fun getCommands(
        agentId: String,
        limit: Int,
    ): Result<List<AgentCommand>> = Result.success(emptyList())
}
