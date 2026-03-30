package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.model.MetricEntity
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.MetricJpaRepository
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
    private lateinit var metricRepository: MetricJpaRepository

    @BeforeEach
    fun setUp() {
        metricRepository.deleteAll()
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
    fun `full aggregation lifecycle - seed metrics then query aggregate`() {
        val now = System.currentTimeMillis()
        val metrics =
            listOf(
                MetricEntity(agentId = "agent-1", name = "cpu_usage", value = 40.0, unit = "percent", timestamp = now - 1000),
                MetricEntity(agentId = "agent-1", name = "cpu_usage", value = 60.0, unit = "percent", timestamp = now - 2000),
                MetricEntity(agentId = "agent-1", name = "cpu_usage", value = 80.0, unit = "percent", timestamp = now - 3000),
                MetricEntity(agentId = "agent-1", name = "cpu_usage", value = 20.0, unit = "percent", timestamp = now - 4000),
                MetricEntity(agentId = "agent-1", name = "cpu_usage", value = 50.0, unit = "percent", timestamp = now - 5000),
            )
        metricRepository.saveAll(metrics)

        mockMvc.get("/api/v1/metrics/agent-1/aggregate") {
            param("startTime", (now - 10000).toString())
            param("endTime", (now + 1000).toString())
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].metricName") { value("cpu_usage") }
            jsonPath("$[0].average") { value(50.0) }
            jsonPath("$[0].min") { value(20.0) }
            jsonPath("$[0].max") { value(80.0) }
            jsonPath("$[0].sampleCount") { value(5) }
        }
    }
}
