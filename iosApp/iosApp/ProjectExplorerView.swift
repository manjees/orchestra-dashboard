import SwiftUI
import Shared

struct ProjectExplorerView: View {
    @StateObject private var viewModel = IOSProjectExplorerViewModel()
    @StateObject private var solveDialogViewModel = IOSSolveDialogViewModel()

    @State private var navigateToPipelineId: String? = nil

    var body: some View {
        NavigationLink(
            destination: Group {
                if let pipelineId = navigateToPipelineId {
                    PipelineMonitorView(pipelineId: pipelineId)
                }
            },
            isActive: Binding(
                get: { navigateToPipelineId != nil },
                set: { active in
                    if !active {
                        navigateToPipelineId = nil
                    }
                }
            )
        ) {
            EmptyView()
        }
        .hidden()

        VStack {
            if viewModel.isLoading {
                ProgressView()
            } else {
                if let error = viewModel.error {
                    Text(error)
                        .foregroundStyle(.red)
                        .font(.caption)
                        .padding(.horizontal)
                }

                if viewModel.projects.isEmpty {
                    Text("No projects registered.")
                        .foregroundStyle(.secondary)
                } else {
                    List {
                        Section("Projects") {
                            ForEach(viewModel.projects, id: \.name) { project in
                                Button {
                                    viewModel.selectProject(project)
                                } label: {
                                    HStack {
                                        VStack(alignment: .leading) {
                                            Text(project.name)
                                                .font(.headline)
                                            Text(project.path)
                                                .font(.caption)
                                                .foregroundStyle(.secondary)
                                        }
                                        Spacer()
                                        if viewModel.selectedProject?.name == project.name {
                                            Image(systemName: "checkmark")
                                                .foregroundStyle(.accentColor)
                                        }
                                    }
                                }
                                .buttonStyle(.plain)
                            }
                        }

                        if viewModel.selectedProject != nil {
                            Section("Issues") {
                                if viewModel.issues.isEmpty {
                                    Text("No open issues")
                                        .foregroundStyle(.secondary)
                                } else {
                                    ForEach(viewModel.issues, id: \.number) { issue in
                                        HStack {
                                            VStack(alignment: .leading) {
                                                Text("#\(issue.number) \(issue.title)")
                                                    .font(.subheadline)
                                            }
                                            Spacer()
                                            Button("Solve") {
                                                if let project = viewModel.selectedProject {
                                                    solveDialogViewModel.open(project: project, issue: issue)
                                                }
                                            }
                                            .buttonStyle(.borderedProminent)
                                            .controlSize(.small)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("Project Explorer")
        .onAppear {
            viewModel.loadInitialData()
        }
        .onChange(of: solveDialogViewModel.resultPipelineId) { pipelineId in
            guard let pipelineId else { return }
            navigateToPipelineId = pipelineId
            solveDialogViewModel.consumeResult()
        }
        .sheet(isPresented: .init(
            get: { solveDialogViewModel.showDialog },
            set: { if !$0 { solveDialogViewModel.close() } }
        )) {
            SolveDialogView(
                issues: viewModel.issues.map { SolveIssueItem(id: Int($0.number), title: $0.title) },
                selectedIssueIds: Binding(
                    get: { solveDialogViewModel.selectedIssueIds },
                    set: { newIds in
                        let diff = newIds.symmetricDifference(solveDialogViewModel.selectedIssueIds)
                        diff.forEach { solveDialogViewModel.toggleIssueSelection(issueNumber: $0) }
                    }
                ),
                selectedMode: Binding(
                    get: { solveDialogViewModel.mode },
                    set: { solveDialogViewModel.setMode($0) }
                ),
                isParallel: Binding(
                    get: { solveDialogViewModel.isParallel },
                    set: { _ in solveDialogViewModel.toggleParallel() }
                ),
                isSolving: solveDialogViewModel.isLoading,
                solveError: solveDialogViewModel.error,
                onSolve: { solveDialogViewModel.executeSolve() },
                onDismiss: { solveDialogViewModel.close() },
                onClearError: { solveDialogViewModel.clearError() }
            )
        }
    }
}

#Preview {
    NavigationView {
        ProjectExplorerView()
    }
}
