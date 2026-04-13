import SwiftUI
import Shared

struct ProjectExplorerView: View {
    @StateObject private var viewModel = IOSProjectExplorerViewModel()

    var body: some View {
        VStack {
            if viewModel.isLoading {
                ProgressView()
            } else {
                Text("Project Explorer")
                    .font(.largeTitle)
                if let error = viewModel.error {
                    Text(error)
                        .foregroundStyle(.red)
                        .font(.caption)
                }
            }
        }
        .navigationTitle("Project Explorer")
        .onAppear {
            viewModel.loadInitialData()
        }
        .sheet(isPresented: .init(
            get: { viewModel.showSolveDialog },
            set: { if !$0 { viewModel.closeSolveDialog() } }
        )) {
            SolveDialogView(
                issues: viewModel.selectedIssueIds.map { SolveIssueItem(id: $0, title: "#\($0)") },
                selectedIssueIds: .init(
                    get: { viewModel.selectedIssueIds },
                    set: { _ in }
                ),
                selectedMode: .init(
                    get: { viewModel.solveMode },
                    set: { viewModel.setSolveMode($0) }
                ),
                isParallel: .init(
                    get: { viewModel.isParallel },
                    set: { _ in viewModel.toggleParallel() }
                ),
                isSolving: viewModel.isSolving,
                solveError: viewModel.solveError,
                onSolve: {
                    viewModel.executeSolve()
                },
                onDismiss: {
                    viewModel.closeSolveDialog()
                }
            )
        }
    }
}

#Preview {
    NavigationView {
        ProjectExplorerView()
    }
}
