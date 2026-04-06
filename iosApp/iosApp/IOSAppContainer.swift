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

    private let orchestratorBaseUrl = ProcessInfo.processInfo.environment["ORCHESTRATOR_URL"] ?? "http://localhost:9000"
    private let orchestratorApiKey = ProcessInfo.processInfo.environment["ORCHESTRATOR_API_KEY"] ?? ""

    // MARK: - Shared Dependencies

    private lazy var solveCommandMapper = SolveCommandMapper()

    private lazy var orchestratorApiClient = OrchestratorApiClient(
        baseUrl: orchestratorBaseUrl,
        apiKey: orchestratorApiKey
    )

    private lazy var solveRepository: any SolveRepository = SolveRepositoryImpl(
        api: orchestratorApiClient,
        mapper: solveCommandMapper
    )

    private lazy var executeSolveUseCase = ExecuteSolveUseCase(repository: solveRepository)

    // MARK: - ViewModel Factories

    func createDashboardViewModel() -> DashboardViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createProjectExplorerViewModel() -> ProjectExplorerViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createDashboardHomeViewModel() -> DashboardHomeViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createPipelineMonitorViewModel(pipelineId: String) -> PipelineMonitorViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }
}
