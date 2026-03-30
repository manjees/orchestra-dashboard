package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.AgentEventEntity
import com.orchestradashboard.server.repository.AgentEventJpaRepository
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.AggregatedMetricJpaRepository
import com.orchestradashboard.server.service.MetricsAggregationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser
class MetricsIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var agentRepository: AgentJpaRepository

    @Autowired
    private lateinit var eventRepository: AgentEventJpaRepository

    @Autowired
    private lateinit var aggregatedMetricRepository: AggregatedMetricJpaRepository

    @Autowired
    private lateinit var metricsAggregationService: MetricsAggregationService

    @BeforeEach
    fun setUp() {
        aggregatedMetricRepository.deleteAll()
        eventRepository.deleteAll()
        agentRepository.deleteAll()
        agentRepository.save(
            AgentEntity(
                id = "agent-1",
                name = "TestAgent",
                type = "WORKER",
                status = "RUNNING",
                lastHeartbeat = System.currentTimeMillis(),
            ),
        )
    }

    @Test
    fun `should return empty data when no metrics exist in range`() {
        mockMvc.get("/api/v1/metrics/agent-1/aggregate") {
            param("startTime", "0")
            param("endTime", "999999999999")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(0) }
        }
    }

    @Test
    fun `full aggregation lifecycle - seed events then query aggregate`() {
        val now = System.currentTimeMillis()
        val events =
            listOf(
                AgentEventEntity(agentId = "agent-1", type = "HEARTBEAT", payload = """{"cpu_usage": 40.0}""", timestamp = now - 1000),
                AgentEventEntity(agentId = "agent-1", type = "HEARTBEAT", payload = """{"cpu_usage": 60.0}""", timestamp = now - 2000),
                AgentEventEntity(agentId = "agent-1", type = "HEARTBEAT", payload = """{"cpu_usage": 80.0}""", timestamp = now - 3000),
                AgentEventEntity(agentId = "agent-1", type = "HEARTBEAT", payload = """{"cpu_usage": 20.0}""", timestamp = now - 4000),
                AgentEventEntity(agentId = "agent-1", type = "HEARTBEAT", payload = """{"cpu_usage": 50.0}""", timestamp = now - 5000),
            )
        eventRepository.saveAll(events)

        metricsAggregationService.aggregateForAgent("agent-1")

        val bucketStart =
            ((now - MetricsAggregationService.BUCKET_DURATION_MS) / MetricsAggregationService.BUCKET_DURATION_MS) *
                MetricsAggregationService.BUCKET_DURATION_MS

        mockMvc.get("/api/v1/metrics/agent-1/aggregate") {
            param("startTime", bucketStart.toString())
            param("endTime", (now + 3_600_000).toString())
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].metric_name") { value("cpu_usage") }
            jsonPath("$[0].avg_value") { value(50.0) }
            jsonPath("$[0].min_value") { value(20.0) }
            jsonPath("$[0].max_value") { value(80.0) }
            jsonPath("$[0].count") { value(5) }
        }
    }
}
