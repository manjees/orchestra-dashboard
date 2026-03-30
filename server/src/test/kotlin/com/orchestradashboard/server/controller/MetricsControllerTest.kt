package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.JwtAuthenticationFilter
import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.config.SecurityConfig
import com.orchestradashboard.server.model.TimeSeriesDataResponse
import com.orchestradashboard.server.service.MetricsAggregationService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.mockito.Mockito.`when` as whenever

@WebMvcTest(MetricsController::class)
@Import(SecurityConfig::class, JwtAuthenticationFilter::class)
@WithMockUser
class MetricsControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var metricsAggregationService: MetricsAggregationService

    @MockBean
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `should return empty data when no metrics exist in range`() {
        val emptyResponse =
            listOf(
                TimeSeriesDataResponse(
                    agentId = "agent-1",
                    metricName = "cpu_usage",
                    dataPoints = emptyList(),
                    average = null,
                    min = null,
                    max = null,
                    sampleCount = 0,
                    fromTimestamp = 100L,
                    toTimestamp = 200L,
                ),
            )
        whenever(metricsAggregationService.getAggregatedMetrics(eq("agent-1"), any(), any(), any()))
            .thenReturn(emptyResponse)

        mockMvc.get("/api/v1/metrics/agent-1/aggregate?from=100&to=200&metricName=cpu_usage")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].dataPoints") { isEmpty() }
                jsonPath("$[0].average") { doesNotExist() }
                jsonPath("$[0].sampleCount") { value(0) }
            }
    }

    @Test
    fun `should handle missing agent ID gracefully`() {
        whenever(metricsAggregationService.getAggregatedMetrics(eq("nonexistent"), isNull(), isNull(), isNull()))
            .thenThrow(NoSuchElementException("Agent with id 'nonexistent' not found"))

        mockMvc.get("/api/v1/metrics/nonexistent/aggregate")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.error") { value("Agent with id 'nonexistent' not found") }
            }
    }

    @Test
    fun `should reject invalid time range`() {
        whenever(metricsAggregationService.getAggregatedMetrics(eq("agent-1"), eq(500L), eq(100L), isNull()))
            .thenThrow(IllegalArgumentException("'from' must be before 'to'"))

        mockMvc.get("/api/v1/metrics/agent-1/aggregate?from=500&to=100")
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.error") { value("'from' must be before 'to'") }
            }
    }

    @Test
    fun `should default to last hour when no range specified`() {
        whenever(metricsAggregationService.getAggregatedMetrics(eq("agent-1"), isNull(), isNull(), isNull()))
            .thenReturn(emptyList())

        mockMvc.get("/api/v1/metrics/agent-1/aggregate")
            .andExpect {
                status { isOk() }
            }

        verify(metricsAggregationService).getAggregatedMetrics(eq("agent-1"), isNull(), isNull(), isNull())
    }

    @Test
    fun `should filter by metric name`() {
        whenever(metricsAggregationService.getAggregatedMetrics(eq("agent-1"), any(), any(), eq("cpu_usage")))
            .thenReturn(emptyList())

        mockMvc.get("/api/v1/metrics/agent-1/aggregate?metricName=cpu_usage")
            .andExpect {
                status { isOk() }
            }

        verify(metricsAggregationService).getAggregatedMetrics(eq("agent-1"), isNull(), isNull(), eq("cpu_usage"))
    }
}
