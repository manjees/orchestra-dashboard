package com.orchestradashboard.desktop.di

import com.orchestradashboard.shared.data.api.OrchestratorApiClient
import com.orchestradashboard.shared.data.mapper.ActivePipelineMapper
import com.orchestradashboard.shared.data.mapper.AgentEventMapper
import com.orchestradashboard.shared.data.mapper.AgentMapper
import com.orchestradashboard.shared.data.mapper.AnalyticsMapper
import com.orchestradashboard.shared.data.mapper.ApprovalMapper
import com.orchestradashboard.shared.data.mapper.CheckpointMapper
import com.orchestradashboard.shared.data.mapper.CommandMapper
import com.orchestradashboard.shared.data.mapper.HistoryDetailMapper
import com.orchestradashboard.shared.data.mapper.IssueMapper
import com.orchestradashboard.shared.data.mapper.MonitoredPipelineMapper
import com.orchestradashboard.shared.data.mapper.NotificationMapper
import com.orchestradashboard.shared.data.mapper.PipelineHistoryMapper
import com.orchestradashboard.shared.data.mapper.PipelineRunMapper
import com.orchestradashboard.shared.data.mapper.ProjectMapper
import com.orchestradashboard.shared.data.mapper.SolveCommandMapper
import com.orchestradashboard.shared.data.mapper.SystemStatusMapper
import com.orchestradashboard.shared.data.network.DashboardApiClient
import com.orchestradashboard.shared.data.repository.AgentRepositoryImpl
import com.orchestradashboard.shared.data.repository.AnalyticsRepositoryImpl
import com.orchestradashboard.shared.data.repository.ApprovalRepositoryImpl
import com.orchestradashboard.shared.data.repository.CheckpointRepositoryImpl
import com.orchestradashboard.shared.data.repository.CommandRepositoryImpl
import com.orchestradashboard.shared.data.repository.DesktopSettingsRepository
import com.orchestradashboard.shared.data.repository.DesktopTokenRepository
import com.orchestradashboard.shared.data.repository.EventRepositoryImpl
import com.orchestradashboard.shared.data.repository.HistoryRepositoryImpl
import com.orchestradashboard.shared.data.repository.MetricRepositoryImpl
import com.orchestradashboard.shared.data.repository.NotificationRepositoryImpl
import com.orchestradashboard.shared.data.repository.PipelineMonitorRepositoryImpl
import com.orchestradashboard.shared.data.repository.PipelineRepositoryImpl
import com.orchestradashboard.shared.data.repository.ProjectRepositoryImpl
import com.orchestradashboard.shared.data.repository.SolveRepositoryImpl
import com.orchestradashboard.shared.data.repository.SystemRepositoryImpl
import com.orchestradashboard.shared.data.repository.TokenRefreshHandler
import com.orchestradashboard.shared.domain.model.DashboardViewModel
import com.orchestradashboard.shared.domain.repository.AgentRepository
import com.orchestradashboard.shared.domain.repository.AnalyticsRepository
import com.orchestradashboard.shared.domain.repository.ApprovalRepository
import com.orchestradashboard.shared.domain.repository.CheckpointRepository
import com.orchestradashboard.shared.domain.repository.CommandRepository
import com.orchestradashboard.shared.domain.repository.EventRepository
import com.orchestradashboard.shared.domain.repository.HistoryRepository
import com.orchestradashboard.shared.domain.repository.MetricRepository
import com.orchestradashboard.shared.domain.repository.NotificationRepository
import com.orchestradashboard.shared.domain.repository.PipelineMonitorRepository
import com.orchestradashboard.shared.domain.repository.PipelineRepository
import com.orchestradashboard.shared.domain.repository.ProjectRepository
import com.orchestradashboard.shared.domain.repository.SettingsRepository
import com.orchestradashboard.shared.domain.repository.SolveRepository
import com.orchestradashboard.shared.domain.repository.SystemRepository
import com.orchestradashboard.shared.domain.repository.TokenRepository
import com.orchestradashboard.shared.domain.usecase.DesignUseCase
import com.orchestradashboard.shared.domain.usecase.DiscussUseCase
import com.orchestradashboard.shared.domain.usecase.ExecuteShellUseCase
import com.orchestradashboard.shared.domain.usecase.ExecuteSolveUseCase
import com.orchestradashboard.shared.domain.usecase.GetActivePipelinesUseCase
import com.orchestradashboard.shared.domain.usecase.GetAgentUseCase
import com.orchestradashboard.shared.domain.usecase.GetAggregatedMetricsUseCase
import com.orchestradashboard.shared.domain.usecase.GetCheckpointsUseCase
import com.orchestradashboard.shared.domain.usecase.GetDurationTrendsUseCase
import com.orchestradashboard.shared.domain.usecase.GetHistoryDetailUseCase
import com.orchestradashboard.shared.domain.usecase.GetNotificationSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.GetPagedHistoryUseCase
import com.orchestradashboard.shared.domain.usecase.GetPipelineAnalyticsUseCase
import com.orchestradashboard.shared.domain.usecase.GetPipelineHistoryUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectIssuesUseCase
import com.orchestradashboard.shared.domain.usecase.GetProjectsUseCase
import com.orchestradashboard.shared.domain.usecase.GetSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.GetStepFailureRatesUseCase
import com.orchestradashboard.shared.domain.usecase.GetSystemStatusUseCase
import com.orchestradashboard.shared.domain.usecase.InitProjectUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveAgentsUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveEventsUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveIncomingNotificationsUseCase
import com.orchestradashboard.shared.domain.usecase.ObservePipelineRunsUseCase
import com.orchestradashboard.shared.domain.usecase.ObserveSystemEventsUseCase
import com.orchestradashboard.shared.domain.usecase.PlanIssuesUseCase
import com.orchestradashboard.shared.domain.usecase.RespondToApprovalUseCase
import com.orchestradashboard.shared.domain.usecase.RetryCheckpointUseCase
import com.orchestradashboard.shared.domain.usecase.SaveNotificationSettingsUseCase
import com.orchestradashboard.shared.domain.usecase.SaveSettingsUseCase
import com.orchestradashboard.shared.push.DesktopNotificationLocalStore
import com.orchestradashboard.shared.push.DesktopPushNotificationProvider
import com.orchestradashboard.shared.ui.agentdetail.AgentDetailViewModel
import com.orchestradashboard.shared.ui.analytics.AnalyticsViewModel
import com.orchestradashboard.shared.ui.approvalmodal.ApprovalModalViewModel
import com.orchestradashboard.shared.ui.commandcenter.CommandCenterViewModel
import com.orchestradashboard.shared.ui.dashboardhome.DashboardHomeViewModel
import com.orchestradashboard.shared.ui.history.HistoryViewModel
import com.orchestradashboard.shared.ui.pipelinemonitor.PipelineMonitorViewModel
import com.orchestradashboard.shared.ui.projectexplorer.ProjectExplorerViewModel
import com.orchestradashboard.shared.ui.settings.SettingsViewModel
import com.orchestradashboard.shared.ui.solvedialog.SolveDialogViewModel
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

    // ─── Settings ──────────────────────────────────────────────────

    val settingsRepository: SettingsRepository by lazy {
        DesktopSettingsRepository()
    }

    private val getSettingsUseCase: GetSettingsUseCase by lazy {
        GetSettingsUseCase(settingsRepository)
    }

    private val saveSettingsUseCase: SaveSettingsUseCase by lazy {
        SaveSettingsUseCase(settingsRepository)
    }

    // ─── Mappers ────────────────────────────────────────────────

    private val agentMapper: AgentMapper by lazy { AgentMapper() }
    private val pipelineRunMapper: PipelineRunMapper by lazy { PipelineRunMapper() }
    private val agentEventMapper: AgentEventMapper by lazy { AgentEventMapper() }
    private val projectMapper: ProjectMapper by lazy { ProjectMapper() }
    private val issueMapper: IssueMapper by lazy { IssueMapper() }
    private val checkpointMapper: CheckpointMapper by lazy { CheckpointMapper() }
    private val systemStatusMapper: SystemStatusMapper by lazy { SystemStatusMapper() }
    private val activePipelineMapper: ActivePipelineMapper by lazy { ActivePipelineMapper() }
    private val pipelineHistoryMapper: PipelineHistoryMapper by lazy { PipelineHistoryMapper() }
    private val monitoredPipelineMapper: MonitoredPipelineMapper by lazy { MonitoredPipelineMapper() }
    private val commandMapper: CommandMapper by lazy { CommandMapper() }
    private val approvalMapper: ApprovalMapper by lazy { ApprovalMapper() }
    private val solveCommandMapper: SolveCommandMapper by lazy { SolveCommandMapper() }
    private val analyticsMapper: AnalyticsMapper by lazy { AnalyticsMapper() }
    private val historyDetailMapper: HistoryDetailMapper by lazy { HistoryDetailMapper() }
    private val notificationMapper: NotificationMapper by lazy { NotificationMapper() }

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

    private val projectRepository: ProjectRepository by lazy {
        ProjectRepositoryImpl(apiClient, projectMapper, issueMapper, checkpointMapper)
    }

    private val checkpointRepository: CheckpointRepository by lazy {
        CheckpointRepositoryImpl(apiClient, checkpointMapper)
    }

    private val solveRepository: SolveRepository by lazy {
        SolveRepositoryImpl(apiClient, solveCommandMapper)
    }

    private val pipelineMonitorRepository: PipelineMonitorRepository by lazy {
        PipelineMonitorRepositoryImpl(apiClient, monitoredPipelineMapper, orchestratorApi = orchestratorApiClient)
    }

    private val systemRepository: SystemRepository by lazy {
        SystemRepositoryImpl(
            apiClient,
            systemStatusMapper,
            activePipelineMapper,
            pipelineHistoryMapper,
            orchestratorApi = orchestratorApiClient,
        )
    }

    private val approvalRepository: ApprovalRepository by lazy {
        ApprovalRepositoryImpl(apiClient)
    }

    private val commandRepository: CommandRepository by lazy {
        CommandRepositoryImpl(apiClient, commandMapper)
    }

    private val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepositoryImpl(apiClient, analyticsMapper)
    }

    private val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(apiClient, historyDetailMapper)
    }

    // ─── Push Notifications ─────────────────────────────────────

    val pushNotificationProvider: DesktopPushNotificationProvider by lazy {
        DesktopPushNotificationProvider()
    }

    private val notificationLocalStore: DesktopNotificationLocalStore by lazy {
        DesktopNotificationLocalStore()
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepositoryImpl(
            api = apiClient,
            mapper = notificationMapper,
            localStore = notificationLocalStore,
            pushProvider = pushNotificationProvider,
        )
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

    private val getProjectsUseCase: GetProjectsUseCase by lazy {
        GetProjectsUseCase(projectRepository)
    }

    private val getProjectIssuesUseCase: GetProjectIssuesUseCase by lazy {
        GetProjectIssuesUseCase(projectRepository)
    }

    private val getCheckpointsUseCase: GetCheckpointsUseCase by lazy {
        GetCheckpointsUseCase(checkpointRepository)
    }

    private val retryCheckpointUseCase: RetryCheckpointUseCase by lazy {
        RetryCheckpointUseCase(checkpointRepository)
    }

    private val executeSolveUseCase: ExecuteSolveUseCase by lazy {
        ExecuteSolveUseCase(solveRepository)
    }

    private val getSystemStatusUseCase: GetSystemStatusUseCase by lazy {
        GetSystemStatusUseCase(systemRepository)
    }

    private val getActivePipelinesUseCase: GetActivePipelinesUseCase by lazy {
        GetActivePipelinesUseCase(systemRepository)
    }

    private val getPipelineHistoryUseCase: GetPipelineHistoryUseCase by lazy {
        GetPipelineHistoryUseCase(systemRepository)
    }

    private val observeSystemEventsUseCase: ObserveSystemEventsUseCase by lazy {
        ObserveSystemEventsUseCase(systemRepository)
    }

    private val initProjectUseCase: InitProjectUseCase by lazy { InitProjectUseCase(commandRepository) }
    private val planIssuesUseCase: PlanIssuesUseCase by lazy { PlanIssuesUseCase(commandRepository) }
    private val discussUseCase: DiscussUseCase by lazy { DiscussUseCase(commandRepository) }
    private val designUseCase: DesignUseCase by lazy { DesignUseCase(commandRepository) }
    private val respondToApprovalUseCase: RespondToApprovalUseCase by lazy {
        RespondToApprovalUseCase(approvalRepository)
    }
    private val executeShellUseCase: ExecuteShellUseCase by lazy { ExecuteShellUseCase(commandRepository) }

    private val getPipelineAnalyticsUseCase: GetPipelineAnalyticsUseCase by lazy {
        GetPipelineAnalyticsUseCase(analyticsRepository)
    }

    private val getStepFailureRatesUseCase: GetStepFailureRatesUseCase by lazy {
        GetStepFailureRatesUseCase(analyticsRepository)
    }

    private val getDurationTrendsUseCase: GetDurationTrendsUseCase by lazy {
        GetDurationTrendsUseCase(analyticsRepository)
    }

    private val getPagedHistoryUseCase: GetPagedHistoryUseCase by lazy {
        GetPagedHistoryUseCase(historyRepository)
    }

    private val getHistoryDetailUseCase: GetHistoryDetailUseCase by lazy {
        GetHistoryDetailUseCase(historyRepository)
    }

    private val getNotificationSettingsUseCase: GetNotificationSettingsUseCase by lazy {
        GetNotificationSettingsUseCase(notificationRepository)
    }

    private val saveNotificationSettingsUseCase: SaveNotificationSettingsUseCase by lazy {
        SaveNotificationSettingsUseCase(notificationRepository)
    }

    val observeIncomingNotificationsUseCase: ObserveIncomingNotificationsUseCase by lazy {
        ObserveIncomingNotificationsUseCase(notificationRepository)
    }

    // ─── ViewModels (new instance per screen lifecycle) ─────────

    fun createDashboardViewModel(): DashboardViewModel =
        DashboardViewModel(observeAgentsUseCase, getAgentUseCase, getAggregatedMetricsUseCase)

    fun createAgentDetailViewModel(agentId: String): AgentDetailViewModel =
        AgentDetailViewModel(agentId, getAgentUseCase, observePipelineRunsUseCase, observeEventsUseCase)

    fun createProjectExplorerViewModel(): ProjectExplorerViewModel =
        ProjectExplorerViewModel(
            getProjectsUseCase,
            getProjectIssuesUseCase,
            getCheckpointsUseCase,
            retryCheckpointUseCase,
        )

    fun createSolveDialogViewModel(): SolveDialogViewModel = SolveDialogViewModel(executeSolveUseCase)

    fun createPipelineMonitorViewModel(pipelineId: String): PipelineMonitorViewModel =
        PipelineMonitorViewModel(
            pipelineId = pipelineId,
            repository = pipelineMonitorRepository,
            approvalModal =
                ApprovalModalViewModel(
                    respondToApprovalUseCase = respondToApprovalUseCase,
                    approvalMapper = approvalMapper,
                ),
        )

    fun createDashboardHomeViewModel(): DashboardHomeViewModel =
        DashboardHomeViewModel(
            getSystemStatusUseCase,
            getActivePipelinesUseCase,
            getPipelineHistoryUseCase,
            observeSystemEventsUseCase,
        )

    fun createCommandCenterViewModel(): CommandCenterViewModel =
        CommandCenterViewModel(
            initProjectUseCase = initProjectUseCase,
            planIssuesUseCase = planIssuesUseCase,
            discussUseCase = discussUseCase,
            designUseCase = designUseCase,
            executeShellUseCase = executeShellUseCase,
            getProjectsUseCase = getProjectsUseCase,
        )

    fun createSettingsViewModel(): SettingsViewModel =
        SettingsViewModel(
            getSettingsUseCase = getSettingsUseCase,
            saveSettingsUseCase = saveSettingsUseCase,
            getNotificationSettingsUseCase = getNotificationSettingsUseCase,
            saveNotificationSettingsUseCase = saveNotificationSettingsUseCase,
        )

    fun createHistoryViewModel(): HistoryViewModel =
        HistoryViewModel(
            getPagedHistoryUseCase = getPagedHistoryUseCase,
            getHistoryDetailUseCase = getHistoryDetailUseCase,
        )

    fun createAnalyticsViewModel(project: String = "default"): AnalyticsViewModel =
        AnalyticsViewModel(
            getPipelineAnalyticsUseCase,
            getDurationTrendsUseCase,
            getStepFailureRatesUseCase,
            project,
        )
}
