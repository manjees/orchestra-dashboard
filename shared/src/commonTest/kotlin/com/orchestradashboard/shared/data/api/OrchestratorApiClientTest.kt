package com.orchestradashboard.shared.data.api

import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.OllamaModelDto
import com.orchestradashboard.shared.data.dto.orchestrator.OllamaStatusDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineStepDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.data.dto.orchestrator.TmuxSessionDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OrchestratorApiClientTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun createClient(
        handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): Pair<HttpClient, OrchestratorApiClient> {
        val engine = MockEngine { request -> handler(request) }
        val httpClient =
            HttpClient(engine) {
                install(ContentNegotiation) { json(json) }
            }
        return httpClient to OrchestratorApiClient(httpClient, "http://localhost:9000")
    }

    private val sampleSystemStatus =
        SystemStatusDto(
            ramTotalGb = 32.0,
            ramUsedGb = 16.5,
            ramPercent = 51.6,
            cpuPercent = 25.0,
            thermalPressure = "nominal",
            diskTotalGb = 500.0,
            diskUsedGb = 250.0,
            diskPercent = 50.0,
            ollama =
                OllamaStatusDto(
                    online = true,
                    models = listOf(OllamaModelDto(name = "llama3", sizeGb = 4.7)),
                ),
            tmuxSessions = listOf(TmuxSessionDto(name = "main", windows = 3, created = "2024-01-01T00:00:00")),
        )

    private val sampleProject =
        ProjectDto(
            name = "my-project",
            path = "/home/user/my-project",
            ciCommands = listOf("./gradlew build"),
            openIssuesCount = 5,
            recentSolves = 2,
        )

    private val sampleProjectDetail =
        ProjectDetailDto(
            name = "my-project",
            path = "/home/user/my-project",
            ciCommands = listOf("./gradlew build"),
            openIssuesCount = 5,
            recentSolves = 2,
            summary = "A sample project",
        )

    private val sampleIssue =
        OrchestratorIssueDto(
            number = 42,
            title = "Fix login bug",
            labels = listOf("bug"),
            state = "open",
            createdAt = "2024-01-15T10:00:00",
        )

    private val samplePipeline =
        OrchestratorPipelineDto(
            id = "pipe-1",
            projectName = "my-project",
            issueNum = 42,
            issueTitle = "Fix login bug",
            mode = "full",
            status = "running",
            currentStep = "implement",
            startedAt = "2024-01-15T10:00:00",
            steps = listOf(PipelineStepDto(name = "implement", status = "running", elapsedSec = 120.0)),
            elapsedTotalSec = 120.0,
        )

    private val sampleCheckpoint =
        CheckpointDto(
            id = "cp-1",
            pipelineId = "pipe-1",
            createdAt = "2024-01-15T10:02:00",
            step = "implement",
            status = "saved",
        )

    private val sampleHistory =
        PipelineHistoryDto(
            id = "pipe-0",
            projectName = "my-project",
            issueNum = 41,
            status = "completed",
            startedAt = "2024-01-14T10:00:00",
            completedAt = "2024-01-14T11:00:00",
            elapsedTotalSec = 3600.0,
        )

    // ─── REST method tests ──────────────────────────────────────

    @Test
    fun `getStatus deserializes SystemStatusDto correctly`() =
        runTest {
            val (_, client) = createClient { respond(json.encodeToString(sampleSystemStatus), headers = jsonHeaders) }
            val result = client.getStatus()
            assertEquals(32.0, result.ramTotalGb)
            assertEquals("nominal", result.thermalPressure)
            assertTrue(result.ollama.online)
            assertEquals(1, result.tmuxSessions.size)
        }

    @Test
    fun `getProjects returns list of ProjectDto`() =
        runTest {
            val (_, client) = createClient { respond(json.encodeToString(listOf(sampleProject)), headers = jsonHeaders) }
            val result = client.getProjects()
            assertEquals(1, result.size)
            assertEquals("my-project", result[0].name)
        }

    @Test
    fun `getProject returns ProjectDetailDto`() =
        runTest {
            val (_, client) = createClient { respond(json.encodeToString(sampleProjectDetail), headers = jsonHeaders) }
            val result = client.getProject("my-project")
            assertEquals("A sample project", result.summary)
        }

    @Test
    fun `getProjectIssues returns list of OrchestratorIssueDto`() =
        runTest {
            val (_, client) = createClient { respond(json.encodeToString(listOf(sampleIssue)), headers = jsonHeaders) }
            val result = client.getProjectIssues("my-project")
            assertEquals(1, result.size)
            assertEquals(42, result[0].number)
        }

    @Test
    fun `getPipelines returns list of OrchestratorPipelineDto`() =
        runTest {
            val (_, client) = createClient { respond(json.encodeToString(listOf(samplePipeline)), headers = jsonHeaders) }
            val result = client.getPipelines()
            assertEquals(1, result.size)
            assertEquals("pipe-1", result[0].id)
        }

    @Test
    fun `getPipeline returns single OrchestratorPipelineDto`() =
        runTest {
            val (_, client) = createClient { respond(json.encodeToString(samplePipeline), headers = jsonHeaders) }
            val result = client.getPipeline("pipe-1")
            assertEquals("running", result.status)
            assertEquals(1, result.steps.size)
        }

    @Test
    fun `getCheckpoints returns list of CheckpointDto`() =
        runTest {
            val (_, client) = createClient { respond(json.encodeToString(listOf(sampleCheckpoint)), headers = jsonHeaders) }
            val result = client.getCheckpoints()
            assertEquals(1, result.size)
            assertEquals("cp-1", result[0].id)
        }

    @Test
    fun `getPipelineHistory returns list of PipelineHistoryDto`() =
        runTest {
            val (_, client) = createClient { respond(json.encodeToString(listOf(sampleHistory)), headers = jsonHeaders) }
            val result = client.getPipelineHistory()
            assertEquals(1, result.size)
            assertEquals("completed", result[0].status)
        }

    // ─── API Key injection tests ────────────────────────────────

    @Test
    fun `every request includes X-API-Key header`() =
        runTest {
            var capturedApiKey: String? = null
            val engine =
                MockEngine { request ->
                    capturedApiKey = request.headers["X-API-Key"]
                    respond(json.encodeToString(listOf(sampleProject)), headers = jsonHeaders)
                }
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) { json(json) }
                }
            val client = OrchestratorApiClient(httpClient, "http://localhost:9000", "test-secret-key")
            client.getProjects()
            assertEquals("test-secret-key", capturedApiKey)
        }

    @Test
    fun `empty API key still sends header`() =
        runTest {
            var capturedApiKey: String? = null
            val engine =
                MockEngine { request ->
                    capturedApiKey = request.headers["X-API-Key"]
                    respond(json.encodeToString(listOf(sampleProject)), headers = jsonHeaders)
                }
            val httpClient =
                HttpClient(engine) {
                    install(ContentNegotiation) { json(json) }
                }
            val client = OrchestratorApiClient(httpClient, "http://localhost:9000", "")
            client.getProjects()
            assertEquals("", capturedApiKey)
        }

    // ─── Error handling tests ───────────────────────────────────

    @Test
    fun `401 response throws OrchestratorUnauthorizedException`() =
        runTest {
            val (_, client) =
                createClient {
                    respond("Unauthorized", status = HttpStatusCode.Unauthorized, headers = jsonHeaders)
                }
            assertFailsWith<OrchestratorUnauthorizedException> { client.getStatus() }
        }

    @Test
    fun `404 response throws OrchestratorNotFoundException`() =
        runTest {
            val (_, client) =
                createClient {
                    respond("Not Found", status = HttpStatusCode.NotFound, headers = jsonHeaders)
                }
            assertFailsWith<OrchestratorNotFoundException> { client.getProject("nonexistent") }
        }

    @Test
    fun `network failure throws OrchestratorNetworkException`() =
        runTest {
            val (_, client) = createClient { throw RuntimeException("Connection refused") }
            assertFailsWith<OrchestratorNetworkException> { client.getStatus() }
        }

    // ─── Request path verification ──────────────────────────────

    @Test
    fun `getStatus sends GET to correct path`() =
        runTest {
            var capturedPath: String? = null
            val (_, client) =
                createClient {
                    capturedPath = it.url.encodedPath
                    respond(json.encodeToString(sampleSystemStatus), headers = jsonHeaders)
                }
            client.getStatus()
            assertEquals("/api/status", capturedPath)
        }

    @Test
    fun `getProjects sends GET to correct path`() =
        runTest {
            var capturedPath: String? = null
            val (_, client) =
                createClient {
                    capturedPath = it.url.encodedPath
                    respond(json.encodeToString(listOf(sampleProject)), headers = jsonHeaders)
                }
            client.getProjects()
            assertEquals("/api/projects", capturedPath)
        }

    @Test
    fun `getProject sends GET to correct path with name`() =
        runTest {
            var capturedPath: String? = null
            val (_, client) =
                createClient {
                    capturedPath = it.url.encodedPath
                    respond(json.encodeToString(sampleProjectDetail), headers = jsonHeaders)
                }
            client.getProject("my-project")
            assertEquals("/api/projects/my-project", capturedPath)
        }

    @Test
    fun `getProjectIssues sends GET to correct path`() =
        runTest {
            var capturedPath: String? = null
            val (_, client) =
                createClient {
                    capturedPath = it.url.encodedPath
                    respond(json.encodeToString(listOf(sampleIssue)), headers = jsonHeaders)
                }
            client.getProjectIssues("my-project")
            assertEquals("/api/projects/my-project/issues", capturedPath)
        }

    @Test
    fun `getPipeline sends GET to correct path with id`() =
        runTest {
            var capturedPath: String? = null
            val (_, client) =
                createClient {
                    capturedPath = it.url.encodedPath
                    respond(json.encodeToString(samplePipeline), headers = jsonHeaders)
                }
            client.getPipeline("pipe-1")
            assertEquals("/api/pipelines/pipe-1", capturedPath)
        }

    @Test
    fun `getPipelineHistory sends GET to correct path`() =
        runTest {
            var capturedPath: String? = null
            val (_, client) =
                createClient {
                    capturedPath = it.url.encodedPath
                    respond(json.encodeToString(listOf(sampleHistory)), headers = jsonHeaders)
                }
            client.getPipelineHistory()
            assertEquals("/api/pipelines/history", capturedPath)
        }
}
