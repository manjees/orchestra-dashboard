package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.PipelineHistoryResponse
import com.orchestradashboard.server.model.StepHistoryResponse
import com.orchestradashboard.server.service.PipelineHistoryService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(PipelineHistoryController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class PipelineHistoryControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var historyService: PipelineHistoryService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    private val sampleResponse =
        PipelineHistoryResponse(
            id = "h-1",
            projectName = "my-project",
            issueNum = 42,
            issueTitle = "Fix bug",
            mode = "solve",
            status = "PASSED",
            startedAt = 1700000000L,
            completedAt = 1700003600L,
            elapsedTotalSec = 3600.0,
            prUrl = "https://github.com/org/repo/pull/1",
            steps =
                listOf(
                    StepHistoryResponse(
                        stepName = "analyze",
                        status = "PASSED",
                        elapsedSec = 120.0,
                        failDetail = null,
                    ),
                ),
        )

    @Test
    fun `GET api-v1-pipeline-history returns 200 with page`() {
        val pageable = PageRequest.of(0, 20)
        whenever(historyService.getHistory(null, null, pageable))
            .thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipeline-history")
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
                jsonPath("$.content[0].id") { value("h-1") }
                jsonPath("$.content[0].project_name") { value("my-project") }
                jsonPath("$.content[0].status") { value("PASSED") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-history with project filter`() {
        val pageable = PageRequest.of(0, 20)
        whenever(historyService.getHistory(eq("my-project"), eq(null), any()))
            .thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipeline-history?project=my-project")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].project_name") { value("my-project") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-history with status filter`() {
        val pageable = PageRequest.of(0, 20)
        whenever(historyService.getHistory(eq(null), eq("PASSED"), any()))
            .thenReturn(PageImpl(listOf(sampleResponse), pageable, 1))

        mockMvc.get("/api/v1/pipeline-history?status=PASSED")
            .andExpect {
                status { isOk() }
                jsonPath("$.content[0].status") { value("PASSED") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-history-id returns 200 with detail`() {
        whenever(historyService.getHistoryById("h-1")).thenReturn(sampleResponse)

        mockMvc.get("/api/v1/pipeline-history/h-1")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value("h-1") }
                jsonPath("$.steps[0].step_name") { value("analyze") }
            }
    }

    @Test
    fun `GET api-v1-pipeline-history-id returns 404 when not found`() {
        whenever(historyService.getHistoryById("missing"))
            .thenThrow(NoSuchElementException("Pipeline history with id 'missing' not found"))

        mockMvc.get("/api/v1/pipeline-history/missing")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `GET api-v1-pipeline-history clamps size to 100`() {
        val clampedPageable = PageRequest.of(0, 100)
        whenever(historyService.getHistory(eq(null), eq(null), eq(clampedPageable)))
            .thenReturn(PageImpl(emptyList(), clampedPageable, 0))

        mockMvc.get("/api/v1/pipeline-history?size=200")
            .andExpect {
                status { isOk() }
            }
    }
}
