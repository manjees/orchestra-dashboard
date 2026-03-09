package com.orchestradashboard.shared.domain.usecase

import com.orchestradashboard.shared.domain.model.Agent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveAgentsUseCaseTest {
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
