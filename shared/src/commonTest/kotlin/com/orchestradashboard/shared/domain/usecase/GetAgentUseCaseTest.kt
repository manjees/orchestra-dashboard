package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Agent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAgentUseCaseTest {
    private val agents =
        listOf(
            Agent("1", "Alpha", Agent.AgentType.WORKER, Agent.AgentStatus.RUNNING, 100L),
            Agent("2", "Beta", Agent.AgentType.PLANNER, Agent.AgentStatus.IDLE, 200L),
        )
    private val repository = FakeAgentRepository(agents)
    private val useCase = GetAgentUseCase(repository)

    @Test
    fun `invoke returns success result for existing agent`() =
        runTest {
            val result = useCase("1")

            assertTrue(result.isSuccess)
            assertEquals(agents[0], result.getOrNull())
        }

    @Test
    fun `invoke returns failure result for non-existent agent`() =
        runTest {
            val result = useCase("nonexistent")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NoSuchElementException)
        }
}
