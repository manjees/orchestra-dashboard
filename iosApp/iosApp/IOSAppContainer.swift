import Foundation
import Shared

/// iOS-side dependency injection container.
/// Mirrors the AppContainer pattern from Android and Desktop.
final class IOSAppContainer {
    static let shared = IOSAppContainer()

    private init() {}

    func createDashboardViewModel() -> DashboardViewModel {
        // Instantiate through the shared KMP module
        // Full implementation requires setting up the KMP framework binary
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }

    func createProjectExplorerViewModel() -> ProjectExplorerViewModel {
        fatalError("KMP framework must be linked via Gradle :shared:iosArm64Binaries")
    }
}
