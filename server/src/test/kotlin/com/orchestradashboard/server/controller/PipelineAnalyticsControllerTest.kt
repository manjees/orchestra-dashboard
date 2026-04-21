package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.DurationTrendResponse
import com.orchestradashboard.server.model.PipelineAnalyticsResponse
import com.orchestradashboard.server.model.StepFailureRateResponse
import com.orchestradashboard.server.service.PipelineAnalyticsService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(PipelineAnalyticsController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
class PipelineAnalyticsControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var analyticsService: PipelineAnalyticsService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `GET api-v1-analytics-pipelines-summary returns 200`() {
        val response =
            PipelineAnalyticsResponse(
                project = "my-project",
                successRate = 0.85,
                avgDurationSec = 120.5,
                totalRuns = 100,
                failedRuns = 15,
            )
        whenever(analyticsService.getSummary("my-project", null, null)).thenReturn(response)

        mockMvc.get("/api/v1/analytics/pipelines/summary?project=my-project")
            .andExpect {
                status { isOk() }
                jsonPath("$.project") { value("my-project") }
                jsonPath("$.success_rate") { value(0.85) }
                jsonPath("$.total_runs") { value(100) }
                jsonPath("$.failed_runs") { value(15) }
            }
    }

    @Test
    fun `GET api-v1-analytics-pipelines-summary with time range`() {
        val response =
            PipelineAnalyticsResponse(
                project = "my-project",
                successRate = 0.9,
                avgDurationSec = 100.0,
                totalRuns = 50,
                failedRuns = 5,
            )
        whenever(analyticsService.getSummary("my-project", 1000L, 2000L)).thenReturn(response)

        mockMvc.get("/api/v1/analytics/pipelines/summary?project=my-project&from=1000&to=2000")
            .andExpect {
                status { isOk() }
                jsonPath("$.total_runs") { value(50) }
            }
    }

    @Test
    fun `GET api-v1-analytics-pipelines-step-failures returns 200`() {
        val failures =
            listOf(
                StepFailureRateResponse(
                    stepName = "build",
                    totalCount = 50,
                    failedCount = 5,
                    failureRate = 0.1,
                ),
            )
        whenever(analyticsService.getStepFailureRates("my-project")).thenReturn(failures)

        mockMvc.get("/api/v1/analytics/pipelines/step-failures?project=my-project")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].step_name") { value("build") }
                jsonPath("$[0].failure_rate") { value(0.1) }
            }
    }

    @Test
    fun `GET api-v1-analytics-pipelines-duration-trends returns 200`() {
        val trends =
            listOf(
                DurationTrendResponse(date = "2024-01-01", avgDurationSec = 120.0, runCount = 10),
            )
        whenever(analyticsService.getDurationTrends("my-project", "day")).thenReturn(trends)

        mockMvc.get("/api/v1/analytics/pipelines/duration-trends?project=my-project")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].date") { value("2024-01-01") }
                jsonPath("$[0].run_count") { value(10) }
            }
    }

    @Test
    fun `GET api-v1-analytics-pipelines-duration-trends with week granularity`() {
        val trends =
            listOf(
                DurationTrendResponse(date = "2024-01-01", avgDurationSec = 150.0, runCount = 30),
            )
        whenever(analyticsService.getDurationTrends("my-project", "week")).thenReturn(trends)

        mockMvc.get("/api/v1/analytics/pipelines/duration-trends?project=my-project&granularity=week")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].run_count") { value(30) }
            }
    }
}
