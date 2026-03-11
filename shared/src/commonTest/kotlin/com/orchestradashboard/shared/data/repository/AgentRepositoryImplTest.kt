package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.mapper.AgentMapper
import com.orchestradashboard.shared.data.network.FakeDashboardApiClient
import com.orchestradashboard.shared.domain.model.Agent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentRepositoryImplTest {
    private val fakeApi = FakeDashboardApiClient()
    private val mapper = AgentMapper()
    private val repository = AgentRepositoryImpl(fakeApi, mapper, pollingIntervalMs = 50L)

    private val sampleAgentDto =
        AgentDto(
            id = "agent-1",
            name = "Worker One",
            type = "WORKER",
            status = "RUNNING",
            lastHeartbeat = 1700000000L,
        )

    @Test
    fun `getAgent returns Result success with mapped domain model on success`() =
        runTest {
            fakeApi.agentResponse = sampleAgentDto

            val result = repository.getAgent("agent-1")

            assertTrue(result.isSuccess)
            assertEquals("agent-1", result.getOrThrow().id)
            assertEquals("Worker One", result.getOrThrow().name)
            assertEquals(Agent.AgentType.WORKER, result.getOrThrow().type)
        }

    @Test
    fun `getAgent returns Result failure on network error`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("Network error")

            val result = repository.getAgent("agent-1")

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }

    @Test
    fun `observeAgents emits agent list from API`() =
        runTest {
            fakeApi.agentsResponse = listOf(sampleAgentDto)

            val agents = repository.observeAgents().first()

            assertEquals(1, agents.size)
            assertEquals("agent-1", agents[0].id)
        }

    @Test
    fun `observeAgents emits periodically`() =
        runTest {
            fakeApi.agentsResponse = listOf(sampleAgentDto)

            val emissions = repository.observeAgents().take(3).toList()

            assertEquals(3, emissions.size)
            assertTrue(fakeApi.getAgentsCallCount >= 3)
        }

    @Test
    fun `getAgentsByStatus filters correctly from API`() =
        runTest {
            fakeApi.agentsResponse =
                listOf(
                    sampleAgentDto,
                    AgentDto("agent-2", "Idle Agent", "WORKER", "IDLE", 0L),
                )

            val result = repository.getAgentsByStatus(Agent.AgentStatus.RUNNING)

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
            assertEquals("agent-1", result.getOrThrow()[0].id)
        }

    @Test
    fun `registerAgent returns Result success with mapped domain model`() =
        runTest {
            fakeApi.registerResponse = sampleAgentDto
            val agent =
                Agent(
                    id = "agent-1",
                    name = "Worker One",
                    type = Agent.AgentType.WORKER,
                    status = Agent.AgentStatus.RUNNING,
                    lastHeartbeat = 1700000000L,
                )

            val result = repository.registerAgent(agent)

            assertTrue(result.isSuccess)
            assertEquals("agent-1", result.getOrThrow().id)
        }

    @Test
    fun `registerAgent returns Result failure on network error`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("Network error")
            val agent =
                Agent(
                    id = "agent-1",
                    name = "Worker One",
                    type = Agent.AgentType.WORKER,
                    status = Agent.AgentStatus.RUNNING,
                    lastHeartbeat = 0L,
                )

            val result = repository.registerAgent(agent)

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }

    @Test
    fun `deregisterAgent returns Result success`() =
        runTest {
            val result = repository.deregisterAgent("agent-1")

            assertTrue(result.isSuccess)
        }

    @Test
    fun `deregisterAgent returns Result failure on network error`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("Network error")

            val result = repository.deregisterAgent("agent-1")

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
        }
}
