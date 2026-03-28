package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.dto.AgentPageDto
import com.orchestradashboard.shared.data.dto.AuthResponseDto
import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.network.DashboardApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeDashboardApiClient(
    private val pollingIntervalMs: Long = 5000L,
) : DashboardApi {
    var agents: List<AgentDto> = emptyList()
    var pagedAgents: AgentPageDto = AgentPageDto(emptyList(), 0, 20, 0, 0)
    var pipelineRuns: List<PipelineRunDto> = emptyList()
    var events: List<AgentEventDto> = emptyList()
    var shouldFail: Boolean = false

    override fun observeAgents(): Flow<List<AgentDto>> =
        flow {
            while (true) {
                if (shouldFail) throw RuntimeException("Network error")
                emit(agents)
                delay(pollingIntervalMs)
            }
        }

    override suspend fun getAgents(): List<AgentDto> {
        if (shouldFail) throw RuntimeException("Network error")
        return agents
    }

    override suspend fun getAgent(agentId: String): AgentDto {
        if (shouldFail) throw RuntimeException("Network error")
        return agents.first { it.id == agentId }
    }

    override suspend fun getPipelineRuns(agentId: String?): List<PipelineRunDto> {
        if (shouldFail) throw RuntimeException("Network error")
        return if (agentId != null) {
            pipelineRuns.filter { it.agentId == agentId }
        } else {
            pipelineRuns
        }
    }

    override suspend fun getPipelineRun(id: String): PipelineRunDto {
        if (shouldFail) throw RuntimeException("Network error")
        return pipelineRuns.first { it.id == id }
    }

    override suspend fun getRecentEvents(
        agentId: String?,
        limit: Int,
    ): List<AgentEventDto> {
        if (shouldFail) throw RuntimeException("Network error")
        val filtered =
            if (agentId != null) {
                events.filter { it.agentId == agentId }
            } else {
                events
            }
        return filtered.take(limit)
    }

    override fun observePipelineRuns(agentId: String?): Flow<List<PipelineRunDto>> =
        flow {
            while (true) {
                if (shouldFail) throw RuntimeException("Network error")
                emit(getPipelineRuns(agentId))
                delay(pollingIntervalMs)
            }
        }

    override fun observeEvents(agentId: String): Flow<List<AgentEventDto>> =
        flow {
            while (true) {
                if (shouldFail) throw RuntimeException("Network error")
                emit(getRecentEvents(agentId = agentId))
                delay(pollingIntervalMs)
            }
        }

    override suspend fun registerAgent(agent: AgentDto): AgentDto {
        if (shouldFail) throw RuntimeException("Network error")
        return agent
    }

    override suspend fun deregisterAgent(agentId: String) {
        if (shouldFail) throw RuntimeException("Network error")
    }

    override suspend fun getAgentsPaged(
        page: Int,
        pageSize: Int,
        status: String?,
    ): AgentPageDto {
        if (shouldFail) throw RuntimeException("Network error")
        return pagedAgents
    }

    override suspend fun login(apiKey: String): AuthResponseDto {
        if (shouldFail) throw RuntimeException("Network error")
        return AuthResponseDto(accessToken = "fake-access", refreshToken = "fake-refresh", expiresIn = 900)
    }

    override suspend fun refreshToken(refreshToken: String): AuthResponseDto {
        if (shouldFail) throw RuntimeException("Network error")
        return AuthResponseDto(accessToken = "fake-access", refreshToken = "fake-refresh", expiresIn = 900)
    }
}
