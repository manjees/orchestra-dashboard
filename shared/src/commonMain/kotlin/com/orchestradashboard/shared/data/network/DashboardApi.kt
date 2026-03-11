package com.orchestradashboard.shared.data.network

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.dto.PipelineRunDto

interface DashboardApi {
    suspend fun getAgents(): List<AgentDto>

    suspend fun getAgent(agentId: String): AgentDto

    suspend fun getPipelineRuns(agentId: String? = null): List<PipelineRunDto>

    suspend fun getPipelineRun(runId: String): PipelineRunDto

    suspend fun getRecentEvents(
        agentId: String? = null,
        limit: Int = 50,
    ): List<AgentEventDto>

    suspend fun registerAgent(agent: AgentDto): AgentDto

    suspend fun deregisterAgent(agentId: String)
}
