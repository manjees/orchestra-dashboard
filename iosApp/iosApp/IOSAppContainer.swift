import Foundation
import Shared

/// iOS-side dependency injection container.
/// Mirrors the AppContainer pattern from Android and Desktop.
final class IOSAppContainer {
    static let shared = IOSAppContainer()

    private init() {}

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

    func createCommandCenterViewModel() -> CommandCenterViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }
}
