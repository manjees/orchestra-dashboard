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
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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
            id = "pipe-1",
            agentId = "agent-1",
            pipelineName = "build-deploy",
            status = "RUNNING",
            steps =
                listOf(
                    PipelineStepResponse(name = "build", status = "PASSED", detail = "ok", elapsedMs = 100L),
                ),
            startedAt = 1700000000L,
            finishedAt = null,
            triggerInfo = "manual",
        )

    @Test
    fun `GET api-v1-pipelines returns 200 with pipeline list`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineService.getPipelines(null, null, pageable)).thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipelines")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.content[0].id") { value("pipe-1") }
                jsonPath("$.content[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `GET api-v1-pipelines with agentId filter returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineService.getPipelines(eq("agent-1"), eq(null), any())).thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipelines?agentId=agent-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].agent_id") { value("agent-1") }
            }
    }

    @Test
    fun `GET api-v1-pipelines with status filter returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineService.getPipelines(eq(null), eq("RUNNING"), any())).thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipelines?status=RUNNING")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `GET api-v1-pipelines with both filters returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(
            pipelineService.getPipelines(eq("agent-1"), eq("RUNNING"), any()),
        ).thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipelines?agentId=agent-1&status=RUNNING")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].id") { value("pipe-1") }
            }
    }

    @Test
    fun `GET api-v1-pipelines with size over 100 clamps to 100`() {
        val clampedPageable = PageRequest.of(0, 100)
        whenever(
            pipelineService.getPipelines(eq(null), eq(null), eq(clampedPageable)),
        ).thenReturn(PageImpl(emptyList(), clampedPageable, 0))

        mockMvc.get("/api/v1/pipelines?size=200")
            .andExpect {
                status { isOk() }
            }

        verify(pipelineService).getPipelines(null, null, clampedPageable)
    }

    @Test
    fun `GET api-v1-pipelines-id returns 200 with pipeline run`() {
        whenever(pipelineService.getPipeline("pipe-1")).thenReturn(sampleResponse)

        mockMvc.get("/api/v1/pipelines/pipe-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.pipeline_name") { value("build-deploy") }
                jsonPath("$.steps[0].name") { value("build") }
            }
    }

    @Test
    fun `GET api-v1-pipelines-id returns 404 when not found`() {
        whenever(pipelineService.getPipeline("missing")).thenThrow(NoSuchElementException("Pipeline not found"))

        mockMvc.get("/api/v1/pipelines/missing")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `POST api-v1-pipelines returns 201 on creation`() {
        val request = CreatePipelineRunRequest(agentId = "agent-1", pipelineName = "build-deploy")
        whenever(pipelineService.createPipeline(any())).thenReturn(sampleResponse.copy(status = "QUEUED"))

        mockMvc.post("/api/v1/pipelines") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("pipe-1") }
            jsonPath("$.status") { value("QUEUED") }
        }
    }

    @Test
    fun `POST api-v1-pipelines returns 404 when agent not found`() {
        whenever(pipelineService.createPipeline(any())).thenThrow(NoSuchElementException("Agent not found"))

        mockMvc.post("/api/v1/pipelines") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(CreatePipelineRunRequest(agentId = "missing", pipelineName = "build"))
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `PUT api-v1-pipelines-id-status returns 200 on status update`() {
        whenever(pipelineService.updateStatus("pipe-1", "PASSED")).thenReturn(sampleResponse.copy(status = "PASSED"))

        mockMvc.put("/api/v1/pipelines/pipe-1/status") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateStatusRequest(status = "PASSED"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("PASSED") }
        }
    }

    @Test
    fun `PUT api-v1-pipelines-id-status returns 404 when pipeline not found`() {
        whenever(pipelineService.updateStatus("missing", "RUNNING")).thenThrow(NoSuchElementException("Pipeline not found"))

        mockMvc.put("/api/v1/pipelines/missing/status") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateStatusRequest(status = "RUNNING"))
        }.andExpect {
            status { isNotFound() }
        }
    }
}
