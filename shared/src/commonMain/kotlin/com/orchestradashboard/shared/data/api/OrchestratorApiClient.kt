package com.orchestradashboard.shared.data.api

import com.orchestradashboard.shared.data.dto.orchestrator.CheckpointDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DesignResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorPipelineDto
import com.orchestradashboard.shared.data.dto.orchestrator.ParallelPipelineGroupDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineEventDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellRequestDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class OrchestratorApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String,
    private val apiKey: String = "",
) : OrchestratorApi {
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    override suspend fun getStatus(): SystemStatusDto = request("/api/status")

    override suspend fun getProjects(): List<ProjectDto> = request("/api/projects")

    override suspend fun getProject(name: String): ProjectDetailDto = request("/api/projects/$name")

    override suspend fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): List<OrchestratorIssueDto> = request("/api/projects/$name/issues?page=$page&page_size=$pageSize")

    override suspend fun getPipelines(): List<OrchestratorPipelineDto> = request("/api/pipelines")

    override suspend fun getPipeline(id: String): OrchestratorPipelineDto = request("/api/pipelines/$id")

    override suspend fun getCheckpoints(): List<CheckpointDto> = request("/api/checkpoints")

    override suspend fun retryCheckpoint(checkpointId: String): CheckpointDto = request("/api/checkpoints/$checkpointId/retry")

    override suspend fun getPipelineHistory(): List<PipelineHistoryDto> = request("/api/pipelines/history")

    override suspend fun getParallelPipelines(parentId: String): ParallelPipelineGroupDto = request("/api/pipelines/$parentId/parallel")

    override fun connectEvents(): Flow<PipelineEventDto> =
        flow {
            httpClient.webSocket(
                request = {
                    url("$baseUrl/ws/events")
                    header("X-API-Key", apiKey)
                },
            ) {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val event = json.decodeFromString<PipelineEventDto>(frame.readText())
                        emit(event)
                    }
                }
            }
        }

    override fun connectEvents(pipelineId: String): Flow<PipelineEventDto> =
        flow {
            httpClient.webSocket(
                request = {
                    url("$baseUrl/ws/events/$pipelineId")
                    header("X-API-Key", apiKey)
                },
            ) {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val event = json.decodeFromString<PipelineEventDto>(frame.readText())
                        emit(event)
                    }
                }
            }
        }

    override suspend fun postInitProject(request: InitProjectRequestDto): InitProjectResponseDto =
        postRequest("/api/commands/init", request)

    override suspend fun postPlanIssues(projectName: String): PlanIssuesResponseDto =
        postRequest("/api/commands/plan", PlanIssuesRequestDto(project = projectName))

    override suspend fun postDiscuss(request: DiscussRequestDto): DiscussResponseDto =
        postRequest("/api/commands/discuss", request)

    override suspend fun postDesign(request: DesignRequestDto): DesignResponseDto =
        postRequest("/api/commands/design", request)

    override suspend fun postShell(request: ShellRequestDto): ShellResponseDto =
        postRequest("/api/commands/shell", request)

    @Suppress("TooGenericExceptionCaught")
    private suspend inline fun <reified T, reified B> postRequest(path: String, body: B): T {
        try {
            val response =
                httpClient.post("$baseUrl$path") {
                    header("X-API-Key", apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(body)
                }
            when (response.status.value) {
                401 -> throw OrchestratorUnauthorizedException()
                404 -> throw OrchestratorNotFoundException("$path not found")
            }
            return response.body()
        } catch (e: OrchestratorApiException) {
            throw e
        } catch (e: Exception) {
            throw OrchestratorNetworkException("Network error: ${e.message}", e)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend inline fun <reified T> request(path: String): T {
        try {
            val response =
                httpClient.get("$baseUrl$path") {
                    header("X-API-Key", apiKey)
                }
            when (response.status.value) {
                401 -> throw OrchestratorUnauthorizedException()
                404 -> throw OrchestratorNotFoundException("$path not found")
            }
            return response.body()
        } catch (e: OrchestratorApiException) {
            throw e
        } catch (e: Exception) {
            throw OrchestratorNetworkException("Network error: ${e.message}", e)
        }
    }
}
