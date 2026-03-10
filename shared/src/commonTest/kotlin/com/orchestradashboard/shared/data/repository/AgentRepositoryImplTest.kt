package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.mapper.AgentMapper
import com.orchestradashboard.shared.domain.model.Agent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentRepositoryImplTest {
    private val agentMapper = AgentMapper()

    private fun createFakeClient() = FakeDashboardApiClient(pollingIntervalMs = 5000L)

    private val sampleAgentDto =
        AgentDto(
            id = "agent-1",
            name = "Alpha",
            type = "WORKER",
            status = "RUNNING",
            lastHeartbeat = 1000L,
            metadata = mapOf("version" to "1.0"),
        )

    @Test
    fun `getAgent returns success with mapped domain model`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.agents = listOf(sampleAgentDto)
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)

            val result = repo.getAgent("agent-1")

            assertTrue(result.isSuccess)
            val agent = result.getOrThrow()
            assertEquals("agent-1", agent.id)
            assertEquals("Alpha", agent.name)
            assertEquals(Agent.AgentType.WORKER, agent.type)
            assertEquals(Agent.AgentStatus.RUNNING, agent.status)
        }

    @Test
    fun `getAgent returns failure on network error`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.shouldFail = true
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)

            val result = repo.getAgent("agent-1")

            assertTrue(result.isFailure)
        }

    @Test
    fun `observeAgents emits agent list`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.agents = listOf(sampleAgentDto)
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)

            val result = repo.observeAgents().first()

            assertEquals(1, result.size)
            assertEquals("agent-1", result[0].id)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeAgents emits periodically`() =
        runTest {
            val fakeClient = FakeDashboardApiClient(pollingIntervalMs = 1000L)
            fakeClient.agents = listOf(sampleAgentDto)
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)

            val emissions = mutableListOf<List<Agent>>()
            val job =
                launch {
                    repo.observeAgents().take(3).toList(emissions)
                }
            advanceTimeBy(3000L)
            job.join()

            assertEquals(3, emissions.size)
        }

    @Test
    fun `observeAgents emits empty list when no agents`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.agents = emptyList()
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)

            val result = repo.observeAgents().first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getAgentsByStatus filters correctly`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.agents =
                listOf(
                    sampleAgentDto,
                    AgentDto("agent-2", "Idle Agent", "WORKER", "IDLE", 0L),
                )
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)

            val result = repo.getAgentsByStatus(Agent.AgentStatus.RUNNING)

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow().size)
            assertEquals("agent-1", result.getOrThrow()[0].id)
        }

    @Test
    fun `registerAgent returns success with mapped domain model`() =
        runTest {
            val fakeClient = createFakeClient()
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)
            val agent =
                Agent(
                    id = "agent-1",
                    name = "Alpha",
                    type = Agent.AgentType.WORKER,
                    status = Agent.AgentStatus.RUNNING,
                    lastHeartbeat = 1000L,
                    metadata = mapOf("version" to "1.0"),
                )

            val result = repo.registerAgent(agent)

            assertTrue(result.isSuccess)
            assertEquals("agent-1", result.getOrThrow().id)
        }

    @Test
    fun `registerAgent returns failure on network error`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.shouldFail = true
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)
            val agent =
                Agent(
                    id = "agent-1",
                    name = "Alpha",
                    type = Agent.AgentType.WORKER,
                    status = Agent.AgentStatus.RUNNING,
                    lastHeartbeat = 0L,
                )

            val result = repo.registerAgent(agent)

            assertTrue(result.isFailure)
        }

    @Test
    fun `deregisterAgent returns success`() =
        runTest {
            val fakeClient = createFakeClient()
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)

            val result = repo.deregisterAgent("agent-1")

            assertTrue(result.isSuccess)
        }

    @Test
    fun `deregisterAgent returns failure on network error`() =
        runTest {
            val fakeClient = createFakeClient()
            fakeClient.shouldFail = true
            val repo = AgentRepositoryImpl(fakeClient, agentMapper)

            val result = repo.deregisterAgent("agent-1")

            assertTrue(result.isFailure)
        }
}
