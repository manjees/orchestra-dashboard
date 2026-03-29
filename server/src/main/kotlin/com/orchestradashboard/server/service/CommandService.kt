package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.AgentCommandEntity
import com.orchestradashboard.server.model.AgentCommandMapper
import com.orchestradashboard.server.model.AgentCommandResponse
import com.orchestradashboard.server.model.CreateCommandRequest
import com.orchestradashboard.server.repository.AgentCommandJpaRepository
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.websocket.AgentEventWebSocketHandler
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CommandService(
    private val commandRepository: AgentCommandJpaRepository,
    private val agentRepository: AgentJpaRepository,
    private val commandMapper: AgentCommandMapper,
    private val webSocketHandler: AgentEventWebSocketHandler,
) {
    companion object {
        val VALID_COMMAND_TYPES = listOf("START", "STOP", "RESTART")
        const val EXECUTING_TIMEOUT_MS = 30_000L
        const val PENDING_TIMEOUT_MS = 60_000L
        val ACTIVE_STATUSES = listOf("PENDING", "EXECUTING")
    }

    fun createCommand(
        request: CreateCommandRequest,
        requestedBy: String,
    ): AgentCommandResponse {
        require(request.commandType in VALID_COMMAND_TYPES) {
            "Invalid command type '${request.commandType}'. Valid: $VALID_COMMAND_TYPES"
        }
        agentRepository.findById(request.agentId)
            .orElseThrow { NoSuchElementException("Agent '${request.agentId}' not found") }

        val activeCommands =
            commandRepository.findByAgentIdAndStatusIn(
                request.agentId,
                ACTIVE_STATUSES,
            )
        check(activeCommands.isEmpty()) {
            "Agent '${request.agentId}' already has an active command (${activeCommands.first().status})"
        }

        val entity =
            AgentCommandEntity(
                id = UUID.randomUUID().toString(),
                agentId = request.agentId,
                commandType = request.commandType,
                status = "PENDING",
                requestedAt = System.currentTimeMillis(),
                requestedBy = requestedBy,
            )
        val response = commandMapper.toResponse(commandRepository.save(entity))
        webSocketHandler.broadcastCommand(response)
        return response
    }

    fun getCommandsByAgentId(
        agentId: String,
        limit: Int? = null,
    ): List<AgentCommandResponse> {
        val effectiveLimit = (limit ?: 20).coerceIn(1, 100)
        return commandMapper.toResponseList(
            commandRepository.findByAgentIdOrderByRequestedAtDesc(agentId, PageRequest.of(0, effectiveLimit)),
        )
    }

    fun timeoutStaleCommands() {
        val now = System.currentTimeMillis()
        val staleExecuting =
            commandRepository.findByStatusAndExecutedAtLessThan(
                "EXECUTING",
                now - EXECUTING_TIMEOUT_MS,
            )
        val stalePending =
            commandRepository.findByStatusAndRequestedAtLessThan(
                "PENDING",
                now - PENDING_TIMEOUT_MS,
            )
        (staleExecuting + stalePending).forEach { cmd ->
            val reason = if (cmd.status == "EXECUTING") "timeout" else "pending_timeout"
            val updated =
                cmd.copy(
                    status = "FAILED",
                    failureReason = reason,
                    completedAt = now,
                )
            val response = commandMapper.toResponse(commandRepository.save(updated))
            webSocketHandler.broadcastCommand(response)
        }
    }
}
