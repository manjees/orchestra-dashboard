package com.orchestradashboard.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.orchestradashboard.server.model.AgentEntity
import com.orchestradashboard.server.repository.AgentJpaRepository
import com.orchestradashboard.server.repository.PipelineRunJpaRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PipelineRunIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var agentRepository: AgentJpaRepository

    @Autowired
    private lateinit var pipelineRepository: PipelineRunJpaRepository

    @BeforeEach
    fun setUp() {
        pipelineRepository.deleteAll()
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
    fun `POST pipeline-runs creates record and returns 201 with snake_case JSON`() {
        mockMvc.post("/api/v1/pipeline-runs") {
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                    "agent_id": "agent-1",
                    "pipeline_name": "test-pipeline",
                    "trigger_info": "#42 Add user authentication"
                }
                """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.agent_id") { value("agent-1") }
            jsonPath("$.pipeline_name") { value("test-pipeline") }
            jsonPath("$.trigger_info") { value("#42 Add user authentication") }
            jsonPath("$.status") { value("QUEUED") }
            jsonPath("$.started_at") { isNumber() }
            jsonPath("$.id") { isNotEmpty() }
        }
    }

    @Test
    fun `GET pipeline-runs returns paginated list`() {
        repeat(3) { i ->
            createPipelineRun(pipelineName = "pipeline-$i")
        }

        mockMvc.get("/api/v1/pipeline-runs") {
            param("page", "0")
            param("size", "10")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(3) }
            jsonPath("$.totalElements") { value(3) }
            jsonPath("$.content[0].agent_id") { value("agent-1") }
            jsonPath("$.content[0].pipeline_name") { isNotEmpty() }
        }
    }

    @Test
    fun `GET pipeline-runs with agentId filter returns only matching runs`() {
        agentRepository.save(
            AgentEntity(
                id = "agent-2",
                name = "OtherAgent",
                type = "ORCHESTRATOR",
                status = "RUNNING",
                lastHeartbeat = System.currentTimeMillis(),
            ),
        )
        createPipelineRun(agentId = "agent-1", pipelineName = "pipeline-a")
        createPipelineRun(agentId = "agent-2", pipelineName = "pipeline-b")

        mockMvc.get("/api/v1/pipeline-runs") {
            param("agentId", "agent-1")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content.length()") { value(1) }
            jsonPath("$.content[0].agent_id") { value("agent-1") }
            jsonPath("$.content[0].pipeline_name") { value("pipeline-a") }
        }
    }

    @Test
    fun `GET pipeline-runs by id returns single run with steps`() {
        val id = createPipelineRunAndReturnId()

        val stepsJson =
            """
            {
                "status": "RUNNING",
                "steps": [
                    {"name": "Research", "status": "PASSED", "detail": "done", "elapsed_ms": 5000}
                ]
            }
            """.trimIndent()
        mockMvc.patch("/api/v1/pipeline-runs/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = stepsJson
        }

        mockMvc.get("/api/v1/pipeline-runs/$id")
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(id) }
                jsonPath("$.agent_id") { value("agent-1") }
                jsonPath("$.status") { value("RUNNING") }
                jsonPath("$.steps.length()") { value(1) }
                jsonPath("$.steps[0].name") { value("Research") }
                jsonPath("$.steps[0].elapsed_ms") { value(5000) }
            }
    }

    @Test
    fun `GET pipeline-runs by id returns 404 for nonexistent id`() {
        mockMvc.get("/api/v1/pipeline-runs/nonexistent-id")
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `PATCH pipeline-runs updates status and steps`() {
        val id = createPipelineRunAndReturnId()

        mockMvc.patch("/api/v1/pipeline-runs/$id") {
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                    "status": "PASSED",
                    "finished_at": 1710001200000,
                    "steps": [
                        {"name": "Haiku Research", "status": "PASSED", "detail": "", "elapsed_ms": 45000},
                        {"name": "Opus Design", "status": "PASSED", "detail": "", "elapsed_ms": 120000}
                    ]
                }
                """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("PASSED") }
            jsonPath("$.finished_at") { value(1710001200000) }
            jsonPath("$.steps.length()") { value(2) }
            jsonPath("$.steps[0].name") { value("Haiku Research") }
            jsonPath("$.steps[0].elapsed_ms") { value(45000) }
            jsonPath("$.steps[1].name") { value("Opus Design") }
            jsonPath("$.steps[1].elapsed_ms") { value(120000) }
        }
    }

    @Test
    fun `POST then PATCH full lifecycle — QUEUED to RUNNING to PASSED`() {
        val id = createPipelineRunAndReturnId()

        mockMvc.get("/api/v1/pipeline-runs/$id")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("QUEUED") }
                jsonPath("$.finished_at") { doesNotExist() }
            }

        mockMvc.patch("/api/v1/pipeline-runs/$id") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"status": "RUNNING"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("RUNNING") }
            jsonPath("$.finished_at") { doesNotExist() }
        }

        mockMvc.patch("/api/v1/pipeline-runs/$id") {
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                    "status": "PASSED",
                    "finished_at": 1710001200000,
                    "steps": [
                        {"name": "Build", "status": "PASSED", "detail": "", "elapsed_ms": 30000},
                        {"name": "Test", "status": "PASSED", "detail": "", "elapsed_ms": 60000}
                    ]
                }
                """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("PASSED") }
            jsonPath("$.finished_at") { value(1710001200000) }
            jsonPath("$.steps.length()") { value(2) }
        }

        mockMvc.get("/api/v1/pipeline-runs/$id")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("PASSED") }
                jsonPath("$.finished_at") { value(1710001200000) }
                jsonPath("$.steps.length()") { value(2) }
            }
    }

    private fun createPipelineRun(
        agentId: String = "agent-1",
        pipelineName: String = "test-pipeline",
    ) {
        mockMvc.post("/api/v1/pipeline-runs") {
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                    "agent_id": "$agentId",
                    "pipeline_name": "$pipelineName",
                    "trigger_info": "test trigger"
                }
                """.trimIndent()
        }.andExpect {
            status { isCreated() }
        }
    }

    private fun createPipelineRunAndReturnId(): String {
        val result =
            mockMvc.post("/api/v1/pipeline-runs") {
                contentType = MediaType.APPLICATION_JSON
                content =
                    """
                    {
                        "agent_id": "agent-1",
                        "pipeline_name": "test-pipeline",
                        "trigger_info": "test trigger"
                    }
                    """.trimIndent()
            }.andExpect {
                status { isCreated() }
            }.andReturn()

        val responseBody = objectMapper.readTree(result.response.contentAsString)
        return responseBody["id"].asText()
    }
}
