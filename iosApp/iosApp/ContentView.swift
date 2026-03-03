import SwiftUI
import Shared

/// Root view for the Orchestra Dashboard iOS app.
/// Bridges the KMP Shared framework into SwiftUI.
struct ContentView: View {
    @StateObject private var viewModel = IOSDashboardViewModel()

    var body: some View {
        NavigationView {
            Group {
                if viewModel.isLoading {
                    ProgressView("Loading agents...")
                } else if viewModel.agents.isEmpty {
                    emptyState
                } else {
                    agentList
                }
            }
            .navigationTitle("Orchestra Dashboard")
        }
        .onAppear { viewModel.startObserving() }
        .onDisappear { viewModel.onCleared() }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.clearError() }
        } message: {
            Text(viewModel.error ?? "")
        }
    }

    private var agentList: some View {
        List(viewModel.agents, id: \.id) { agent in
            AgentRow(agent: agent)
        }
    }

    private var emptyState: some View {
        Text("No agents registered.\nStart an agent to see it here.")
            .multilineTextAlignment(.center)
            .foregroundStyle(.secondary)
    }
}

/// Row view for a single agent in the list.
private struct AgentRow: View {
    let agent: Agent

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(agent.displayName)
                .font(.headline)
            Text("Status: \(agent.status.name)")
                .font(.subheadline)
                .foregroundStyle(agent.isHealthy ? Color.green : Color.red)
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    ContentView()
}
