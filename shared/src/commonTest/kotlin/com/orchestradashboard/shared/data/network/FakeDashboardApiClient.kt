package com.orchestradashboard.shared.data.network

import com.orchestradashboard.shared.data.dto.AgentCommandDto
import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.dto.AgentPageDto
import com.orchestradashboard.shared.data.dto.AggregatedMetricDto
import com.orchestradashboard.shared.data.dto.AuthResponseDto
import com.orchestradashboard.shared.data.dto.PipelineRunDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeDashboardApiClient : DashboardApi {
    var agentsResponse: List<AgentDto> = emptyList()
    var agentResponse: AgentDto? = null
    var pipelineRunsResponse: List<PipelineRunDto> = emptyList()
    var pipelineRunResponse: PipelineRunDto? = null
    var eventsResponse: List<AgentEventDto> = emptyList()
    var registerResponse: AgentDto? = null

    var errorToThrow: Exception? = null

    var getAgentsCallCount = 0
        private set
    var getAgentCallCount = 0
        private set
    var getPipelineRunsCallCount = 0
        private set
    var getPipelineRunCallCount = 0
        private set
    var getRecentEventsCallCount = 0
        private set

    private fun maybeThrow() {
        errorToThrow?.let { throw it }
    }

    override fun observeAgents(): Flow<List<AgentDto>> =
        flow {
            while (true) {
                maybeThrow()
                emit(getAgents())
                delay(50L)
            }
        }

    override suspend fun getAgents(): List<AgentDto> {
        getAgentsCallCount++
        maybeThrow()
        return agentsResponse
    }

    override suspend fun getAgent(agentId: String): AgentDto {
        getAgentCallCount++
        maybeThrow()
        return agentResponse ?: throw NoSuchElementException("Agent $agentId not found")
    }

    override suspend fun getPipelineRuns(agentId: String?): List<PipelineRunDto> {
        getPipelineRunsCallCount++
        maybeThrow()
        return if (agentId != null) {
            pipelineRunsResponse.filter { it.agentId == agentId }
        } else {
            pipelineRunsResponse
        }
    }

    override suspend fun getPipelineRun(runId: String): PipelineRunDto {
        getPipelineRunCallCount++
        maybeThrow()
        return pipelineRunResponse ?: throw NoSuchElementException("Run $runId not found")
    }

    override suspend fun getRecentEvents(
        agentId: String?,
        limit: Int,
    ): List<AgentEventDto> {
        getRecentEventsCallCount++
        maybeThrow()
        val filtered =
            if (agentId != null) {
                eventsResponse.filter { it.agentId == agentId }
            } else {
                eventsResponse
            }
        return filtered.take(limit)
    }

    override fun observePipelineRuns(agentId: String?): Flow<List<PipelineRunDto>> =
        flow {
            while (true) {
                maybeThrow()
                emit(getPipelineRuns(agentId))
                delay(50L)
            }
        }

    override fun observeEvents(agentId: String): Flow<List<AgentEventDto>> =
        flow {
            while (true) {
                maybeThrow()
                emit(getRecentEvents(agentId = agentId))
                delay(50L)
            }
        }

    override suspend fun registerAgent(agent: AgentDto): AgentDto {
        maybeThrow()
        return registerResponse ?: agent
    }

    override suspend fun deregisterAgent(agentId: String) {
        maybeThrow()
    }

    override suspend fun getAgentsPaged(
        page: Int,
        pageSize: Int,
        status: String?,
    ): AgentPageDto {
        maybeThrow()
        return AgentPageDto(agentsResponse, page, pageSize, agentsResponse.size.toLong(), 1)
    }

    override suspend fun login(apiKey: String): AuthResponseDto {
        maybeThrow()
        return AuthResponseDto(accessToken = "fake-access", refreshToken = "fake-refresh", expiresIn = 900)
    }

    override suspend fun refreshToken(refreshToken: String): AuthResponseDto {
        maybeThrow()
        return AuthResponseDto(accessToken = "fake-access", refreshToken = "fake-refresh", expiresIn = 900)
    }

    override suspend fun sendCommand(
        agentId: String,
        commandType: String,
    ): AgentCommandDto {
        maybeThrow()
        return AgentCommandDto(
            id = "cmd-1",
            agentId = agentId,
            commandType = commandType,
            status = "PENDING",
            requestedAt = 1000L,
            requestedBy = "test-user",
        )
    }

    override suspend fun getCommands(
        agentId: String,
        limit: Int,
    ): List<AgentCommandDto> {
        maybeThrow()
        return emptyList()
    }

    override suspend fun getAggregatedMetrics(
        agentId: String,
        startTime: Long,
        endTime: Long,
    ): List<AggregatedMetricDto> {
        maybeThrow()
        return emptyList()
    }
}
