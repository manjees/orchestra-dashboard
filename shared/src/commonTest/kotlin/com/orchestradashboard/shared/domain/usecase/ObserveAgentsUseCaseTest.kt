package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Agent
import com.orchestradashboard.shared.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAgentsUseCaseTest {
    // ─── Fake implementation ───────────────────────────────────

    private class FakeAgentRepository(
        private val agents: List<Agent> = emptyList(),
        private val error: Throwable? = null,
    ) : AgentRepository {
        override fun observeAgents(): Flow<List<Agent>> {
            if (error != null) throw error
            return flowOf(agents)
        }

        override suspend fun getAgent(agentId: String): Result<Agent> {
            return agents.find { it.id == agentId }
                ?.let { Result.success(it) }
                ?: Result.failure(NoSuchElementException("Agent $agentId not found"))
        }

        override suspend fun registerAgent(agent: Agent): Result<Agent> = Result.success(agent)

        override suspend fun deregisterAgent(agentId: String): Result<Unit> = Result.success(Unit)
    }

    // ─── Tests ─────────────────────────────────────────────────

    @Test
    fun `invoke returns flow emitting the agent list`() =
        runTest {
            val expected =
                listOf(
                    Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
                    Agent("2", "Beta", Agent.AgentType.PLANNER, Agent.AgentStatus.IDLE, 200L),
                )
            val useCase = ObserveAgentsUseCase(FakeAgentRepository(agents = expected))

            val result = useCase().first()

            assertEquals(expected, result)
        }

    @Test
    fun `invoke returns empty list when no agents are registered`() =
        runTest {
            val useCase = ObserveAgentsUseCase(FakeAgentRepository(agents = emptyList()))

            val result = useCase().first()

            assertTrue(result.isEmpty())
        }
}
