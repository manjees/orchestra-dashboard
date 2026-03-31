package com.orchestradashboard.shared.data.repository

import com.orchestradashboard.shared.data.api.FakeOrchestratorApiClient
import com.orchestradashboard.shared.data.dto.orchestrator.OllamaModelDto
import com.orchestradashboard.shared.data.dto.orchestrator.OllamaStatusDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineStepDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.data.dto.orchestrator.TmuxSessionDto
import com.orchestradashboard.shared.data.mapper.ActivePipelineMapper
import com.orchestradashboard.shared.data.mapper.PipelineHistoryMapper
import com.orchestradashboard.shared.data.mapper.SystemStatusMapper
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.ThermalPressure
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SystemRepositoryImplTest {
    private val fakeApi = FakeOrchestratorApiClient()
    private val repository =
        SystemRepositoryImpl(
            api = fakeApi,
            statusMapper = SystemStatusMapper(),
            pipelineMapper = ActivePipelineMapper(),
            historyMapper = PipelineHistoryMapper(),
        )

    private val testStatusDto =
        SystemStatusDto(
            ramTotalGb = 16.0,
            ramUsedGb = 12.0,
            ramPercent = 75.0,
            cpuPercent = 50.0,
            thermalPressure = "nominal",
            diskTotalGb = 500.0,
            diskUsedGb = 300.0,
            diskPercent = 60.0,
            ollama = OllamaStatusDto(online = true, models = listOf(OllamaModelDto("llama3", 4.0))),
            tmuxSessions = listOf(TmuxSessionDto("main", 1, "2024-01-01T00:00:00Z")),
        )

    private val testPipelineDto =
        OrchestratorPipelineDto(
            id = "pipe-1",
            projectName = "proj",
            issueNum = 1,
            issueTitle = "Title",
            mode = "solve",
            status = "RUNNING",
            currentStep = "build",
            startedAt = "2024-01-01T00:00:00Z",
            steps = listOf(PipelineStepDto("build", "RUNNING", 60.0)),
            elapsedTotalSec = 120.0,
        )

    private val testHistoryDto =
        PipelineHistoryDto(
            id = "hist-1",
            projectName = "proj",
            issueNum = 1,
            status = "PASSED",
            startedAt = "2024-01-01T00:00:00Z",
            completedAt = "2024-01-01T01:00:00Z",
            elapsedTotalSec = 300.0,
        )

    @Test
    fun `getSystemStatus returns mapped SystemStatus on success`() =
        runTest {
            fakeApi.statusResult = testStatusDto

            val result = repository.getSystemStatus()

            assertTrue(result.isSuccess)
            val status = result.getOrThrow()
            assertEquals(75.0, status.ramPercent)
            assertEquals(50.0, status.cpuPercent)
            assertEquals(60.0, status.diskPercent)
            assertEquals(ThermalPressure.NOMINAL, status.thermalPressure)
        }

    @Test
    fun `getSystemStatus returns failure when API throws`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("API error")

            val result = repository.getSystemStatus()

            assertTrue(result.isFailure)
        }

    @Test
    fun `getActivePipelines returns mapped list on success`() =
        runTest {
            fakeApi.pipelinesResult = listOf(testPipelineDto)

            val result = repository.getActivePipelines()

            assertTrue(result.isSuccess)
            val pipelines = result.getOrThrow()
            assertEquals(1, pipelines.size)
            assertEquals("pipe-1", pipelines[0].id)
            assertEquals("proj", pipelines[0].projectName)
        }

    @Test
    fun `getActivePipelines returns failure when API throws`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("API error")

            val result = repository.getActivePipelines()

            assertTrue(result.isFailure)
        }

    @Test
    fun `getPipelineHistory returns mapped list on success`() =
        runTest {
            fakeApi.pipelineHistoryResult = listOf(testHistoryDto)

            val result = repository.getPipelineHistory()

            assertTrue(result.isSuccess)
            val history = result.getOrThrow()
            assertEquals(1, history.size)
            assertEquals("hist-1", history[0].id)
            assertEquals(PipelineRunStatus.PASSED, history[0].status)
        }

    @Test
    fun `getPipelineHistory returns failure when API throws`() =
        runTest {
            fakeApi.errorToThrow = RuntimeException("API error")

            val result = repository.getPipelineHistory()

            assertTrue(result.isFailure)
        }

    @Test
    fun `observeSystemEvents returns mapped flow from connectEvents`() =
        runTest {
            fakeApi.eventsResult =
                listOf(
                    PipelineEventDto(
                        type = "heartbeat",
                        ramPercent = 80.0,
                        cpuPercent = 60.0,
                        thermal = "moderate",
                    ),
                )

            val events = repository.observeSystemEvents().toList()

            assertEquals(1, events.size)
            assertEquals(80.0, events[0].ramPercent)
            assertEquals(60.0, events[0].cpuPercent)
            assertEquals("moderate", events[0].thermal)
        }
}
