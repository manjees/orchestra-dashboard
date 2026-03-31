import Foundation
import Shared

/// iOS-specific wrapper around the KMP ProjectExplorerViewModel.
/// Bridges Kotlin coroutine Flows to Swift's ObservableObject/Combine.
@MainActor
final class IOSProjectExplorerViewModel: ObservableObject {
    @Published var projects: [Project] = []
    @Published var selectedProject: Project? = nil
    @Published var issues: [Issue] = []
    @Published var isLoading: Bool = false
    @Published var isLoadingIssues: Bool = false
    @Published var error: String? = nil

    private let viewModel: ProjectExplorerViewModel

    init() {
        let container = IOSAppContainer.shared
        self.viewModel = container.createProjectExplorerViewModel()
        collectUiState()
    }

    private func collectUiState() {
        Task { @MainActor [weak self] in
            guard let self else { return }
            for await state in viewModel.uiState {
                self.projects = state.projects as? [Project] ?? []
                self.selectedProject = state.selectedProject
                self.issues = state.issues as? [Issue] ?? []
                self.isLoading = state.isLoading as! Bool
                self.isLoadingIssues = state.isLoadingIssues as! Bool
                self.error = state.error
            }
        }
    }

    func loadProjects() {
        isLoading = true
        viewModel.loadProjects()
    }

    func selectProject(_ project: Project?) {
        viewModel.selectProject(project: project)
    }

    func refresh() {
        viewModel.refresh()
    }

    func clearError() {
        error = nil
        viewModel.clearError()
    }

    func onCleared() {
        viewModel.onCleared()
    }
}
