package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
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
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(PipelineController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
@WithMockUser
class PipelineControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var pipelineService: PipelineService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

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

    // --- Existing tests updated to use /pipeline-runs path ---

    @Test
    fun `GET api-v1-pipeline-runs returns 200 with pipeline list`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineService.getPipelines(null, null, pageable)).thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipeline-runs")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.content[0].id") { value("pipe-1") }
                jsonPath("$.content[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-runs with agentId filter returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineService.getPipelines(eq("agent-1"), eq(null), any())).thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipeline-runs?agentId=agent-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].agent_id") { value("agent-1") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-runs with status filter returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(pipelineService.getPipelines(eq(null), eq("RUNNING"), any())).thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipeline-runs?status=RUNNING")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].status") { value("RUNNING") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-runs with both filters returns filtered results`() {
        val pageable = PageRequest.of(0, 20)
        whenever(
            pipelineService.getPipelines(eq("agent-1"), eq("RUNNING"), any()),
        ).thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipeline-runs?agentId=agent-1&status=RUNNING")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].id") { value("pipe-1") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-runs with size over 100 clamps to 100`() {
        val clampedPageable = PageRequest.of(0, 100)
        whenever(
            pipelineService.getPipelines(eq(null), eq(null), eq(clampedPageable)),
        ).thenReturn(PageImpl(emptyList(), clampedPageable, 0))

        mockMvc.get("/api/v1/pipeline-runs?size=200")
            .andExpect {
                status { isOk() }
            }

        verify(pipelineService).getPipelines(null, null, clampedPageable)
    }

    @Test
    fun `GET api-v1-pipeline-runs-id returns 200 with pipeline run`() {
        whenever(pipelineService.getPipeline("pipe-1")).thenReturn(sampleResponse)

        mockMvc.get("/api/v1/pipeline-runs/pipe-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.pipeline_name") { value("build-deploy") }
                jsonPath("$.steps[0].name") { value("build") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-runs-id returns 404 when not found`() {
        whenever(pipelineService.getPipeline("missing")).thenThrow(NoSuchElementException("Pipeline not found"))

        mockMvc.get("/api/v1/pipeline-runs/missing")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `POST api-v1-pipeline-runs returns 201 on creation`() {
        whenever(pipelineService.createPipeline(any())).thenReturn(sampleResponse.copy(status = "QUEUED"))

        mockMvc.post("/api/v1/pipeline-runs") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "pipeline_name": "build-deploy"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value("pipe-1") }
            jsonPath("$.status") { value("QUEUED") }
        }
    }

    @Test
    fun `POST api-v1-pipeline-runs returns 404 when agent not found`() {
        whenever(pipelineService.createPipeline(any())).thenThrow(NoSuchElementException("Agent not found"))

        mockMvc.post("/api/v1/pipeline-runs") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "missing", "pipeline_name": "build"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `PUT api-v1-pipeline-runs-id-status returns 200 on status update`() {
        whenever(pipelineService.updateStatus("pipe-1", "PASSED")).thenReturn(sampleResponse.copy(status = "PASSED"))

        mockMvc.put("/api/v1/pipeline-runs/pipe-1/status") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateStatusRequest(status = "PASSED"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("PASSED") }
        }
    }

    @Test
    fun `PUT api-v1-pipeline-runs-id-status returns 404 when pipeline not found`() {
        whenever(pipelineService.updateStatus("missing", "RUNNING")).thenThrow(NoSuchElementException("Pipeline not found"))

        mockMvc.put("/api/v1/pipeline-runs/missing/status") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(UpdateStatusRequest(status = "RUNNING"))
        }.andExpect {
            status { isNotFound() }
        }
    }

    // --- Phase 6: PATCH controller + validation tests (written ahead, will fail until implemented) ---

    @Test
    fun `PATCH api-v1-pipeline-runs-id returns 200 on update`() {
        whenever(pipelineService.updatePipeline(eq("pipe-1"), any())).thenReturn(sampleResponse.copy(status = "PASSED"))

        mockMvc.patch("/api/v1/pipeline-runs/pipe-1") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status": "PASSED"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("PASSED") }
        }
    }

    @Test
    fun `PATCH api-v1-pipeline-runs-id returns 404 when not found`() {
        whenever(pipelineService.updatePipeline(eq("missing"), any())).thenThrow(NoSuchElementException("Pipeline not found"))

        mockMvc.patch("/api/v1/pipeline-runs/missing") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status": "RUNNING"}"""
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `PATCH api-v1-pipeline-runs-id returns 409 on concurrent modification`() {
        whenever(pipelineService.updatePipeline(eq("pipe-1"), any())).thenThrow(
            org.springframework.orm.ObjectOptimisticLockingFailureException("PipelineRunEntity", "pipe-1"),
        )

        mockMvc.patch("/api/v1/pipeline-runs/pipe-1") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status": "PASSED"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.error") { value("Concurrent modification detected. Please retry.") }
        }
    }

    @Test
    fun `PATCH api-v1-pipeline-runs-id returns 400 when status is invalid`() {
        whenever(pipelineService.updatePipeline(eq("pipe-1"), any())).thenThrow(
            IllegalArgumentException("Invalid status 'INVALID'"),
        )

        mockMvc.patch("/api/v1/pipeline-runs/pipe-1") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status": "INVALID"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.error") { exists() }
        }
    }

    @Test
    fun `PATCH api-v1-pipeline-runs-id accepts partial updates`() {
        val updatedResponse =
            sampleResponse.copy(
                steps = listOf(PipelineStepResponse(name = "build", status = "PASSED", detail = "ok", elapsedMs = 200L)),
            )
        whenever(pipelineService.updatePipeline(eq("pipe-1"), any())).thenReturn(updatedResponse)

        mockMvc.patch("/api/v1/pipeline-runs/pipe-1") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"steps": [{"name": "build", "status": "PASSED", "detail": "ok", "elapsed_ms": 200}]}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.steps[0].elapsed_ms") { value(200) }
        }
    }

    // --- Pipeline validation tests ---

    @Test
    fun `POST api-v1-pipeline-runs returns 400 when agent_id is blank`() {
        mockMvc.post("/api/v1/pipeline-runs") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "", "pipeline_name": "build"}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.agentId") { value("agent_id must not be blank") }
        }
    }

    @Test
    fun `POST api-v1-pipeline-runs returns 400 when pipeline_name is blank`() {
        mockMvc.post("/api/v1/pipeline-runs") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"agent_id": "agent-1", "pipeline_name": ""}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.pipelineName") { value("pipeline_name must not be blank") }
        }
    }
}
