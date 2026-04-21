package com.orchestradashboard.server.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
@Suppress("TooManyFunctions")
class OrchestratorProxyService(
    private val orchestratorWebClient: WebClient,
) {
    @Cacheable("projects")
    fun getProjects(): String =
        orchestratorWebClient.get()
            .uri("/api/projects")
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "[]"

    fun getProject(name: String): String =
        orchestratorWebClient.get()
            .uri("/api/projects/{name}", name)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun getProjectIssues(
        name: String,
        page: Int,
        pageSize: Int,
    ): String =
        orchestratorWebClient.get()
            .uri("/api/projects/{name}/issues?page={page}&page_size={pageSize}", name, page, pageSize)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "[]"

    @Cacheable("systemStatus")
    fun getSystemStatus(): String =
        orchestratorWebClient.get()
            .uri("/api/status")
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun getPipelines(): String =
        orchestratorWebClient.get()
            .uri("/api/pipelines")
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "[]"

    fun getPipeline(id: String): String =
        orchestratorWebClient.get()
            .uri("/api/pipelines/{id}", id)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun getPipelineHistory(): String =
        orchestratorWebClient.get()
            .uri("/api/pipelines/history")
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "[]"

    fun getParallelPipelines(parentId: String): String =
        orchestratorWebClient.get()
            .uri("/api/pipelines/{parentId}/parallel", parentId)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun postSolve(body: String): String =
        orchestratorWebClient.post()
            .uri("/api/commands/solve")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun postInit(body: String): String =
        orchestratorWebClient.post()
            .uri("/api/commands/init")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun postPlan(body: String): String =
        orchestratorWebClient.post()
            .uri("/api/commands/plan")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun postDiscuss(body: String): String =
        orchestratorWebClient.post()
            .uri("/api/commands/discuss")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun postDesign(body: String): String =
        orchestratorWebClient.post()
            .uri("/api/commands/design")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun postShell(body: String): String =
        orchestratorWebClient.post()
            .uri("/api/commands/shell")
            .bodyValue(body)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun getCheckpoints(): String =
        orchestratorWebClient.get()
            .uri("/api/checkpoints")
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "[]"

    fun retryCheckpoint(checkpointId: String): String =
        orchestratorWebClient.post()
            .uri("/api/checkpoints/{id}/retry", checkpointId)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"

    fun respondToApproval(
        approvalId: String,
        body: String,
    ): String =
        orchestratorWebClient.post()
            .uri("/api/approvals/{id}/respond", approvalId)
            .bodyValue(body)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: "{}"
}
