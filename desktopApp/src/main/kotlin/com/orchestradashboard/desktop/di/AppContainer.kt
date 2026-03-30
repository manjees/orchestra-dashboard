package com.orchestradashboard.desktop.di

import com.orchestradashboard.shared.data.api.OrchestratorApiClient
import com.orchestradashboard.shared.data.mapper.AgentEventMapper
import com.orchestradashboard.shared.data.mapper.AgentMapper
import com.orchestradashboard.shared.data.mapper.PipelineRunMapper
import com.orchestradashboard.shared.data.network.DashboardApiClient
import com.orchestradashboard.shared.data.repository.AgentRepositoryImpl
import com.orchestradashboard.shared.data.repository.DesktopTokenRepository
import com.orchestradashboard.shared.data.repository.EventRepositoryImpl
import com.orchestradashboard.shared.data.repository.MetricRepositoryImpl
import com.orchestradashboard.shared.data.repository.PipelineRepositoryImpl
import com.orchestradashboard.shared.data.repository.TokenRefreshHandler
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.repository.EventRepository
import com.orchestradashboard.shared.domain.repository.MetricRepository
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import com.orchestradashboard.shared.domain.repository.TokenRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.GetAggregatedMetricsUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
import com.orchestradashboard.shared.domain.usecase.ObservePipelineRunsUseCase
import com.orchestradashboard.shared.ui.agentdetail.AgentDetailViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Manual dependency injection container for the Desktop app.
 */
object AppContainer {
    // ─── Configuration ──────────────────────────────────────────

    private val serverBaseUrl: String
        get() = System.getenv("ORCHESTRATOR_API_URL") ?: "http://localhost:8080"

    // ─── Network ────────────────────────────────────────────────

    private val httpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    },
                )
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }

    private val apiClient: DashboardApiClient by lazy {
        DashboardApiClient(httpClient, serverBaseUrl)
    }

    // ─── Orchestrator Network ───────────────────────────────────

    private val orchestratorBaseUrl: String
        get() = System.getenv("ORCHESTRATOR_URL") ?: "http://localhost:9000"

    private val orchestratorApiKey: String
        get() = System.getenv("ORCHESTRATOR_API_KEY") ?: ""

    private val orchestratorHttpClient: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    },
                )
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(WebSockets)
            defaultRequest {
                header("X-API-Key", orchestratorApiKey)
            }
        }
    }

    val orchestratorApiClient: OrchestratorApiClient by lazy {
        OrchestratorApiClient(orchestratorHttpClient, orchestratorBaseUrl, orchestratorApiKey)
    }

    // ─── Auth ────────────────────────────────────────────────────

    val tokenRepository: TokenRepository by lazy {
        DesktopTokenRepository()
    }

    val tokenRefreshHandler: TokenRefreshHandler by lazy {
        TokenRefreshHandler(httpClient, serverBaseUrl, tokenRepository)
    }

    // ─── Mappers ────────────────────────────────────────────────

    private val agentMapper: AgentMapper by lazy { AgentMapper() }
    private val pipelineRunMapper: PipelineRunMapper by lazy { PipelineRunMapper() }
    private val agentEventMapper: AgentEventMapper by lazy { AgentEventMapper() }

    // ─── Repositories ───────────────────────────────────────────

    val agentRepository: AgentRepository by lazy {
        AgentRepositoryImpl(apiClient, agentMapper)
    }

    private val pipelineRepository: PipelineRepository by lazy {
        PipelineRepositoryImpl(apiClient, pipelineRunMapper)
    }

    private val eventRepository: EventRepository by lazy {
        EventRepositoryImpl(apiClient, agentEventMapper)
    }

    private val metricRepository: MetricRepository by lazy {
        MetricRepositoryImpl(apiClient)
    }

    // ─── UseCases ───────────────────────────────────────────────

    private val observeAgentsUseCase: ObserveAgentsUseCase by lazy {
        ObserveAgentsUseCase(agentRepository)
    }

    private val getAgentUseCase: GetAgentUseCase by lazy {
        GetAgentUseCase(agentRepository)
    }

    private val observePipelineRunsUseCase: ObservePipelineRunsUseCase by lazy {
        ObservePipelineRunsUseCase(pipelineRepository)
    }

    private val observeEventsUseCase: ObserveEventsUseCase by lazy {
        ObserveEventsUseCase(eventRepository)
    }

    private val getAggregatedMetricsUseCase: GetAggregatedMetricsUseCase by lazy {
        GetAggregatedMetricsUseCase(metricRepository)
    }

    // ─── ViewModels (new instance per screen lifecycle) ─────────

    fun createDashboardViewModel(): DashboardViewModel =
        DashboardViewModel(observeAgentsUseCase, getAgentUseCase, getAggregatedMetricsUseCase)

    fun createAgentDetailViewModel(agentId: String): AgentDetailViewModel =
        AgentDetailViewModel(agentId, getAgentUseCase, observePipelineRunsUseCase, observeEventsUseCase)
}
