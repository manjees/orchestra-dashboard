package com.orchestradashboard.android.di

import com.orchestradashboard.shared.data.mapper.AgentMapper
import com.orchestradashboard.shared.data.network.DashboardApiClient
import com.orchestradashboard.shared.data.repository.AgentRepositoryImpl
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Manual dependency injection container for the Android app.
 * All dependencies are lazily initialized and singletons unless noted.
 */
object AppContainer {
    // ─── Configuration ──────────────────────────────────────────

    private val serverBaseUrl: String
        get() = System.getenv("ORCHESTRATOR_API_URL") ?: "http://localhost:8080"

    // ─── Network ────────────────────────────────────────────────

    private val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
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

    // ─── Mappers ────────────────────────────────────────────────

    private val agentMapper: AgentMapper by lazy { AgentMapper() }

    // ─── Repositories ───────────────────────────────────────────

    val agentRepository: AgentRepository by lazy {
        AgentRepositoryImpl(apiClient, agentMapper)
    }

    // ─── UseCases ───────────────────────────────────────────────

    private val observeAgentsUseCase: ObserveAgentsUseCase by lazy {
        ObserveAgentsUseCase(agentRepository)
    }

    private val getAgentUseCase: GetAgentUseCase by lazy {
        GetAgentUseCase(agentRepository)
    }

    // ─── ViewModels (new instance per screen lifecycle) ─────────

    fun createDashboardViewModel(): DashboardViewModel = DashboardViewModel(observeAgentsUseCase, getAgentUseCase)
}
