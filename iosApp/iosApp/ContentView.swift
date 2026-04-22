import SwiftUI
import Shared

/// Root view for the Orchestra Dashboard iOS app.
/// Uses TabView for main navigation across Home, Projects, Commands, and Settings.
struct ContentView: View {
    @StateObject private var networkMonitor = NetworkMonitor()

    var body: some View {
        VStack(spacing: 0) {
            NetworkStatusBannerView(isConnected: networkMonitor.isConnected)

            TabView {
                DashboardHomeView()
                    .tabItem {
                        Label("Home", systemImage: "house")
                    }

                ProjectExplorerView()
                    .tabItem {
                        Label("Projects", systemImage: "folder")
                    }

                CommandCenterView()
                    .tabItem {
                        Label("Commands", systemImage: "terminal")
                    }

                HistoryView()
                    .tabItem {
                        Label("History", systemImage: "clock")
                    }

                SettingsView()
                    .tabItem {
                        Label("Settings", systemImage: "gearshape")
                    }
            }
        }
    }
}

#Preview {
    ContentView()
}
