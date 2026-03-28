package com.orchestradashboard.shared.data.network

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.dto.AuthResponseDto
import com.orchestradashboard.shared.data.dto.PipelineRunDto
import kotlinx.coroutines.flow.Flow

/**
 * Contract for the Dashboard API surface consumed by repository implementations.
 */
interface DashboardApi {
    fun observeAgents(): Flow<List<AgentDto>>

    suspend fun getAgents(): List<AgentDto>

    suspend fun getAgent(agentId: String): AgentDto

    suspend fun getPipelineRuns(agentId: String? = null): List<PipelineRunDto>

    suspend fun getPipelineRun(runId: String): PipelineRunDto

    suspend fun getRecentEvents(
        agentId: String? = null,
        limit: Int = 50,
    ): List<AgentEventDto>

    fun observePipelineRuns(agentId: String? = null): Flow<List<PipelineRunDto>>

    fun observeEvents(agentId: String): Flow<List<AgentEventDto>>

    suspend fun registerAgent(agent: AgentDto): AgentDto

    suspend fun deregisterAgent(agentId: String)

    suspend fun login(apiKey: String): AuthResponseDto

    suspend fun refreshToken(refreshToken: String): AuthResponseDto
}
