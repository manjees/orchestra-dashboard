package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.dto.AgentDto
import com.orchestradashboard.shared.data.dto.AgentPageDto
import com.orchestradashboard.shared.data.mapper.AgentMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentRepositoryPaginationTest {
    private val agentMapper = AgentMapper()
    private val sampleDto =
        AgentDto(
            id = "agent-1",
            name = "Alpha",
            type = "WORKER",
            status = "RUNNING",
            lastHeartbeat = 1000L,
        )

    @Test
    fun `should return cached data immediately on subsequent fetches`() =
        runTest {
            val fake = FakeDashboardApiClient()
            fake.pagedAgents = AgentPageDto(listOf(sampleDto), page = 0, pageSize = 10, totalElements = 1, totalPages = 1)
            val repo = AgentRepositoryImpl(fake, agentMapper)

            val first = repo.observeAgents(page = 0, pageSize = 10).first()
            fake.pagedAgents = AgentPageDto(emptyList(), page = 0, pageSize = 10, totalElements = 0, totalPages = 0)
            val second = repo.observeAgents(page = 0, pageSize = 10).first()

            assertEquals(1, first.agents.size)
            assertEquals(1, second.agents.size)
        }

    @Test
    fun `should invalidate cache when agent status changes via WebSocket`() =
        runTest {
            val fake = FakeDashboardApiClient()
            fake.pagedAgents = AgentPageDto(listOf(sampleDto), page = 0, pageSize = 10, totalElements = 1, totalPages = 1)
            val repo = AgentRepositoryImpl(fake, agentMapper)

            assertEquals("RUNNING", repo.observeAgents(page = 0, pageSize = 10).first().agents[0].status.name)

            fake.pagedAgents =
                AgentPageDto(listOf(sampleDto.copy(status = "IDLE")), page = 0, pageSize = 10, totalElements = 1, totalPages = 1)
            repo.invalidateCache()

            assertEquals("IDLE", repo.observeAgents(page = 0, pageSize = 10).first().agents[0].status.name)
        }

    @Test
    fun `should handle empty pages gracefully`() =
        runTest {
            val fake = FakeDashboardApiClient()
            fake.pagedAgents = AgentPageDto(emptyList(), page = 5, pageSize = 10, totalElements = 25, totalPages = 3)
            val repo = AgentRepositoryImpl(fake, agentMapper)

            val result = repo.observeAgents(page = 5, pageSize = 10).first()
            assertTrue(result.agents.isEmpty())
            assertEquals(25L, result.totalElements)
            assertEquals(3, result.totalPages)
        }
}
