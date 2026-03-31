import SwiftUI
import Shared

/// Root view for the Dashboard Home screen.
/// Displays system health, active pipelines, recent results, and quick actions.
struct DashboardHomeView: View {
    @StateObject private var viewModel = IOSDashboardHomeViewModel()

    var body: some View {
        NavigationView {
            Group {
                if viewModel.isLoading {
                    ProgressView("Loading dashboard...")
                } else {
                    ScrollView {
                        VStack(spacing: 16) {
                            if let status = viewModel.systemStatus {
                                SystemHealthBarView(status: status)
                            }

                            QuickActionsBarView(
                                onNewSolve: { /* navigate */ },
                                onViewProjects: { /* navigate */ }
                            )

                            if !viewModel.activePipelines.isEmpty {
                                SectionHeaderView(title: "Active Pipelines")
                                ForEach(viewModel.activePipelines, id: \.id) { pipeline in
                                    ActivePipelineCardView(pipeline: pipeline)
                                }
                            }

                            if !viewModel.recentResults.isEmpty {
                                SectionHeaderView(title: "Recent Results")
                                ForEach(viewModel.recentResults, id: \.id) { result in
                                    PipelineResultRowView(result: result)
                                }
                            }

                            if viewModel.activePipelines.isEmpty &&
                               viewModel.recentResults.isEmpty {
                                emptyState
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Dashboard")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink("Projects") {
                        ProjectExplorerView()
                    }
                }
            }
        }
        .onAppear {
            viewModel.loadInitialData()
            viewModel.startObserving()
        }
        .onDisappear { viewModel.onCleared() }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.clearError() }
        } message: {
            Text(viewModel.error ?? "")
        }
    }

    private var emptyState: some View {
        VStack(spacing: 8) {
            Text("No pipelines yet.")
                .font(.body)
            Text("Start a solve to see activity here.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
    }
}

private struct SectionHeaderView: View {
    let title: String

    var body: some View {
        HStack {
            Text(title)
                .font(.headline)
            Spacer()
        }
    }
}

#Preview {
    DashboardHomeView()
}
