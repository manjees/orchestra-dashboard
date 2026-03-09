package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.CreatePipelineRunRequest
import com.orchestradashboard.server.model.PipelineRunResponse
import com.orchestradashboard.server.model.PipelineStepResponse
import com.orchestradashboard.server.model.UpdateStatusRequest
import com.orchestradashboard.server.service.PipelineService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(PipelineController::class)
@Import(SecurityConfig::class)
class PipelineControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var pipelineService: PipelineService

    private val sampleResponse =
        PipelineRunResponse(
            id = "run-1",
            agentId = "agent-1",
            pipelineName = "CI Pipeline",
            status = "RUNNING",
            steps = listOf(PipelineStepResponse(name = "Build", status = "PASSED", detail = "OK", elapsedMs = 1200L)),
            startedAt = 1700000000L,
            finishedAt = null,
            triggerInfo = "manual",
        )

    @Test
    fun `GET api-v1-pipelines returns 200 with pipeline list`() {
        whenever(pipelineService.getAllPipelineRuns(eq(null), eq(null), any())).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/pipelines")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$[0].id") { value("run-1") }
                jsonPath("$[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `GET api-v1-pipelines with agentId param returns filtered list`() {
        whenever(pipelineService.getAllPipelineRuns(eq("agent-1"), eq(null), any())).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/pipelines?agentId=agent-1")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].agent_id") { value("agent-1") }
            }
    }

    @Test
    fun `GET api-v1-pipelines with status param returns filtered list`() {
        whenever(pipelineService.getAllPipelineRuns(eq(null), eq("RUNNING"), any())).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/pipelines?status=RUNNING")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `GET api-v1-pipelines with both params returns combined filter`() {
        whenever(pipelineService.getAllPipelineRuns(eq("agent-1"), eq("RUNNING"), any())).thenReturn(listOf(sampleResponse))

        mockMvc.get("/api/v1/pipelines?agentId=agent-1&status=RUNNING")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value("run-1") }
            }
    }

    @Test
    fun `GET api-v1-pipelines supports pagination params`() {
        whenever(pipelineService.getAllPipelineRuns(eq(null), eq(null), any())).thenReturn(emptyList())

        mockMvc.get("/api/v1/pipelines?page=1&size=10")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `GET api-v1-pipelines-id returns 200 when found`() {
        whenever(pipelineService.getPipelineRun("run-1")).thenReturn(sampleResponse)

        mockMvc.get("/api/v1/pipelines/run-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.pipeline_name") { value("CI Pipeline") }
            }
    }

    @Test
    fun `GET api-v1-pipelines-id returns 404 when not found`() {
        whenever(pipelineService.getPipelineRun("missing")).thenThrow(NoSuchElementException("Not found"))

        mockMvc.get("/api/v1/pipelines/missing")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `POST api-v1-pipelines returns 201 on creation`() {
        val request = CreatePipelineRunRequest(agentId = "agent-1", pipelineName = "CI Pipeline", triggerInfo = "manual")
        whenever(pipelineService.createPipelineRun(request)).thenReturn(sampleResponse)

        mockMvc.post("/api/v1/pipelines") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("run-1") }
        }
    }

    @Test
    fun `POST api-v1-pipelines returns 404 when agent not found`() {
        val request = CreatePipelineRunRequest(agentId = "missing", pipelineName = "Pipeline")
        whenever(pipelineService.createPipelineRun(request)).thenThrow(NoSuchElementException("Agent not found"))

        mockMvc.post("/api/v1/pipelines") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `PUT api-v1-pipelines-id-status returns 200`() {
        val updated = sampleResponse.copy(status = "PASSED", finishedAt = 1700001000L)
        whenever(pipelineService.updateStatus("run-1", "PASSED")).thenReturn(updated)

        mockMvc.put("/api/v1/pipelines/run-1/status") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateStatusRequest(status = "PASSED"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("PASSED") }
        }
    }

    @Test
    fun `PUT api-v1-pipelines-id-status returns 404 when not found`() {
        whenever(pipelineService.updateStatus("missing", "PASSED")).thenThrow(NoSuchElementException("Not found"))

        mockMvc.put("/api/v1/pipelines/missing/status") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateStatusRequest(status = "PASSED"))
        }.andExpect {
            status { isNotFound() }
        }
    }
}
