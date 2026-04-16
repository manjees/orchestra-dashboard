import SwiftUI
import Shared

struct ProjectExplorerView: View {
    @StateObject private var viewModel = IOSProjectExplorerViewModel()
    @StateObject private var solveDialogViewModel = IOSSolveDialogViewModel()

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
            get: { solveDialogViewModel.showDialog },
            set: { if !$0 { solveDialogViewModel.close() } }
        )) {
            SolveDialogView(
                issues: solveDialogViewModel.selectedIssueIds.map { SolveIssueItem(id: $0, title: "#\($0)") },
                selectedIssueIds: .init(
                    get: { solveDialogViewModel.selectedIssueIds },
                    set: { _ in }
                ),
                selectedMode: .init(
                    get: { solveDialogViewModel.mode },
                    set: { solveDialogViewModel.setMode($0) }
                ),
                isParallel: .init(
                    get: { solveDialogViewModel.isParallel },
                    set: { _ in solveDialogViewModel.toggleParallel() }
                ),
                isSolving: solveDialogViewModel.isLoading,
                solveError: solveDialogViewModel.error,
                onSolve: {
                    solveDialogViewModel.executeSolve()
                },
                onDismiss: {
                    solveDialogViewModel.close()
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
