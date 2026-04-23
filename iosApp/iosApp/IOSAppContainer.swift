import Foundation
import Shared

/// iOS-side dependency injection container.
/// Mirrors the AppContainer pattern from Android and Desktop.
/// NOTE: All factory methods require the KMP framework to be linked via Gradle.
/// Run `./gradlew :shared:iosArm64Binaries` (or the relevant arch task) before building.
final class IOSAppContainer {
    static let shared = IOSAppContainer()

    private init() {}

    // MARK: - Configuration

    private let serverBaseUrl = ProcessInfo.processInfo.environment["ORCHESTRATOR_API_URL"] ?? "http://localhost:8080"
    private let orchestratorBaseUrl = ProcessInfo.processInfo.environment["ORCHESTRATOR_URL"] ?? "http://localhost:9000"
    private let orchestratorApiKey = ProcessInfo.processInfo.environment["ORCHESTRATOR_API_KEY"] ?? ""

    // MARK: - Shared Dependencies

    private lazy var solveCommandMapper = SolveCommandMapper()

    // NOTE: BFF client (DashboardApiClient) should be used for most API calls.
    // OrchestratorApiClient is retained only for WebSocket event connections.
    private lazy var orchestratorApiClient = OrchestratorApiClient(
        baseUrl: orchestratorBaseUrl,
        apiKey: orchestratorApiKey
    )

    // TODO: Replace with DashboardApiClient once KMP framework is linked
    private lazy var solveRepository: any SolveRepository = SolveRepositoryImpl(
        api: orchestratorApiClient,
        mapper: solveCommandMapper
    )

    private lazy var executeSolveUseCase = ExecuteSolveUseCase(repository: solveRepository)

    // MARK: - Settings

    private lazy var settingsRepository: any SettingsRepository = IOSSettingsRepository()

    private lazy var getSettingsUseCase = GetSettingsUseCase(repository: settingsRepository)

    private lazy var saveSettingsUseCase = SaveSettingsUseCase(repository: settingsRepository)

    // MARK: - Notifications (local-only; APNs stub)
    // TODO: APNs integration deferred — requires Apple Developer paid account.
    // Track in a follow-up issue. Until then, notification push/unregister are stubs
    // and local UNUserNotificationCenter alerts are driven via Shared framework.

    private lazy var pushNotificationProvider = IOSPushNotificationProvider()

    // MARK: - ViewModel Factories

    func createDashboardViewModel() -> DashboardViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createProjectExplorerViewModel() -> ProjectExplorerViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createSolveDialogViewModel() -> SolveDialogViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createDashboardHomeViewModel() -> DashboardHomeViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createPipelineMonitorViewModel(pipelineId: String) -> PipelineMonitorViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createApprovalModalViewModel() -> ApprovalModalViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createCommandCenterViewModel() -> CommandCenterViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createSettingsViewModel() -> SettingsViewModel {
        // TODO: Replace stub NotificationRepository with a wired implementation
        // once APNs integration lands. For now local-only toggles persist via
        // NSUserDefaults via IOSNotificationLocalStore.
        let notificationRepo = IOSNotificationRepositoryStub(
            localStore: IOSNotificationLocalStore(),
            pushProvider: pushNotificationProvider
        )
        return SettingsViewModel(
            getSettingsUseCase: getSettingsUseCase,
            saveSettingsUseCase: saveSettingsUseCase,
            getNotificationSettingsUseCase: GetNotificationSettingsUseCase(repository: notificationRepo),
            saveNotificationSettingsUseCase: SaveNotificationSettingsUseCase(repository: notificationRepo)
        )
    }

    func createHistoryViewModel() -> HistoryViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createAnalyticsViewModel(project: String) -> AnalyticsViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }
}
