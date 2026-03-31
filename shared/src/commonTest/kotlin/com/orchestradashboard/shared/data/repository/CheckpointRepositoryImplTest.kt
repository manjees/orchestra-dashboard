package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.FakeOrchestratorApiClient
import com.orchestradashboard.shared.data.api.OrchestratorNetworkException
import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.mapper.CheckpointMapper
import com.orchestradashboard.shared.domain.model.CheckpointStatus
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckpointRepositoryImplTest {
    private val mapper = CheckpointMapper()
    private val fakeApi = FakeOrchestratorApiClient()

    private fun createRepository() = CheckpointRepositoryImpl(fakeApi, mapper)

    private val failedCheckpoint = CheckpointDto("cp-1", "pipe-1", "2025-01-15T10:30:00Z", "build", "failed")
    private val passedCheckpoint = CheckpointDto("cp-2", "pipe-2", "2025-01-15T11:00:00Z", "test", "passed")
    private val runningCheckpoint = CheckpointDto("cp-3", "pipe-3", "2025-01-15T11:30:00Z", "deploy", "running")

    @Test
    fun `getFailedCheckpoints returns only FAILED checkpoints filtered from API`() =
        runTest {
            fakeApi.checkpointsResult = listOf(failedCheckpoint, passedCheckpoint, runningCheckpoint)
            val repo = createRepository()

            val result = repo.getFailedCheckpoints()

            assertTrue(result.isSuccess)
            val checkpoints = result.getOrThrow()
            assertEquals(1, checkpoints.size)
            assertEquals("cp-1", checkpoints[0].id)
            assertEquals(CheckpointStatus.FAILED, checkpoints[0].status)
        }

    @Test
    fun `getFailedCheckpoints returns failure on network error`() =
        runTest {
            fakeApi.errorToThrow = OrchestratorNetworkException("Connection refused")
            val repo = createRepository()

            val result = repo.getFailedCheckpoints()

            assertTrue(result.isFailure)
        }

    @Test
    fun `retryCheckpoint calls API and returns success Result`() =
        runTest {
            fakeApi.retryCheckpointResult = failedCheckpoint.copy(status = "running")
            val repo = createRepository()

            val result = repo.retryCheckpoint("cp-1")

            assertTrue(result.isSuccess)
            assertEquals("cp-1", result.getOrThrow().id)
            assertEquals(CheckpointStatus.RUNNING, result.getOrThrow().status)
            assertEquals("cp-1", fakeApi.lastRetriedCheckpointId)
        }

    @Test
    fun `retryCheckpoint returns failure on API error`() =
        runTest {
            fakeApi.errorToThrow = OrchestratorNetworkException("Server error")
            val repo = createRepository()

            val result = repo.retryCheckpoint("cp-1")

            assertTrue(result.isFailure)
        }

    @Test
    fun `getFailedCheckpoints returns empty list when no failed checkpoints exist`() =
        runTest {
            fakeApi.checkpointsResult = listOf(passedCheckpoint, runningCheckpoint)
            val repo = createRepository()

            val result = repo.getFailedCheckpoints()

            assertTrue(result.isSuccess)
            assertTrue(result.getOrThrow().isEmpty())
        }
}
