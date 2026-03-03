package com.orchestradashboard.shared.data.network

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.domain.model.Agent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * HTTP and WebSocket client for the Orchestra Dashboard server.
 *
 * @param httpClient Preconfigured Ktor [HttpClient]
 * @param baseUrl Base URL of the server (e.g., "http://localhost:8080")
 */
class DashboardApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Fetches the list of all registered agents (polling-based flow).
     * Replace with WebSocket implementation for true real-time updates.
     *
     * @return [Flow] emitting agent DTO lists
     */
    fun agentUpdates(): Flow<List<AgentDto>> = flow {
        val agents: List<AgentDto> = httpClient.get("$baseUrl/api/v1/agents").body()
        emit(agents)
    }

    /**
     * Fetches a specific agent by ID.
     *
     * @param agentId Unique agent identifier
     * @return [Result] containing the agent DTO on success
     */
    suspend fun fetchAgent(agentId: String): Result<AgentDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/agents/$agentId").body()
    }

    /**
     * Registers a new agent with the server.
     *
     * @param agent Domain model to register
     * @return [Result] containing the created agent DTO on success
     */
    suspend fun registerAgent(agent: Agent): Result<AgentDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/agents") {
            contentType(ContentType.Application.Json)
            setBody(agent)
        }.body()
    }

    /**
     * Deregisters an agent from the monitoring system.
     *
     * @param agentId Unique identifier of the agent to remove
     * @return [Result] containing Unit on success
     */
    suspend fun deregisterAgent(agentId: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/api/v1/agents/$agentId")
        Unit
    }
}
