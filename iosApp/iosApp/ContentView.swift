import SwiftUI
import Shared

/// Root view for the Orchestra Dashboard iOS app.
/// Bridges the KMP Shared framework into SwiftUI.
struct ContentView: View {
    var body: some View {
        DashboardHomeView()
    }
}

#Preview {
    ContentView()
}
