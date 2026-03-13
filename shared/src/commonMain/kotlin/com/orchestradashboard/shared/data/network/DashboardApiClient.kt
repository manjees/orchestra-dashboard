package com.orchestradashboard.shared.data.network

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentEventDto
import com.orchestradashboard.shared.data.dto.AuthResponseDto
import com.orchestradashboard.shared.data.dto.LoginRequestDto
import com.orchestradashboard.shared.data.dto.PipelineRunDto
import com.orchestradashboard.shared.data.dto.PipelineRunPageDto
import com.orchestradashboard.shared.data.dto.RefreshRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DashboardApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val pollingIntervalMs: Long = 5000L,
) : DashboardApi {
    override fun observeAgents(): Flow<List<AgentDto>> =
        flow {
            while (true) {
                emit(getAgents())
                delay(pollingIntervalMs)
            }
        }

    override suspend fun getAgents(): List<AgentDto> {
        return httpClient.get("$baseUrl/api/v1/agents").body()
    }

    override suspend fun getAgent(agentId: String): AgentDto {
        return httpClient.get("$baseUrl/api/v1/agents/$agentId").body()
    }

    override suspend fun getPipelineRuns(agentId: String?): List<PipelineRunDto> {
        val page: PipelineRunPageDto =
            httpClient.get("$baseUrl/api/v1/pipelines") {
                if (agentId != null) parameter("agentId", agentId)
            }.body()
        return page.content
    }

    override suspend fun getPipelineRun(runId: String): PipelineRunDto {
        return httpClient.get("$baseUrl/api/v1/pipelines/$runId").body()
    }

    override suspend fun getRecentEvents(
        agentId: String?,
        limit: Int,
    ): List<AgentEventDto> {
        return httpClient.get("$baseUrl/api/v1/events") {
            if (agentId != null) parameter("agentId", agentId)
            parameter("limit", limit)
        }.body()
    }

    override fun observePipelineRuns(agentId: String?): Flow<List<PipelineRunDto>> =
        flow {
            while (true) {
                emit(getPipelineRuns(agentId))
                delay(pollingIntervalMs)
            }
        }

    override fun observeEvents(agentId: String): Flow<List<AgentEventDto>> =
        flow {
            while (true) {
                emit(getRecentEvents(agentId = agentId))
                delay(pollingIntervalMs)
            }
        }

    override suspend fun registerAgent(agent: AgentDto): AgentDto {
        return httpClient.post("$baseUrl/api/v1/agents") {
            contentType(ContentType.Application.Json)
            setBody(agent)
        }.body()
    }

    override suspend fun deregisterAgent(agentId: String) {
        httpClient.delete("$baseUrl/api/v1/agents/$agentId")
    }

    override suspend fun login(apiKey: String): AuthResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(apiKey))
        }.body()
    }

    override suspend fun refreshToken(refreshToken: String): AuthResponseDto {
        return httpClient.post("$baseUrl/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequestDto(refreshToken))
        }.body()
    }
}
