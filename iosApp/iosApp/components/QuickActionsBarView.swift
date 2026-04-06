import SwiftUI

/// Quick action buttons for "New Solve", "View Projects", and "Command Center".
struct QuickActionsBarView: View {
    let onNewSolve: () -> Void
    let onViewProjects: () -> Void
    let onCommandCenter: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Button(action: onNewSolve) {
                Label("New Solve", systemImage: "play.fill")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)

            Button(action: onViewProjects) {
                Label("View Projects", systemImage: "folder")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)

            Button(action: onCommandCenter) {
                Label("Command Center", systemImage: "terminal")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
        }
    }
}
