import Foundation
import Shared

/// iOS-specific wrapper around the KMP CommandCenterViewModel.
/// Bridges Kotlin coroutine StateFlow to Swift's ObservableObject/Combine.
@MainActor
final class IOSCommandCenterViewModel: ObservableObject {
    @Published var activeTab: CommandTab = .iNIT
    @Published var projects: [Project] = []
    @Published var isLoadingProjects: Bool = false

    // Init Project
    @Published var initName: String = ""
    @Published var initDescription: String = ""
    @Published var initVisibility: ProjectVisibility = .pUBLIC
    @Published var isInitLoading: Bool = false
    @Published var initResult: CommandResult? = nil

    // Plan Issues
    @Published var planSelectedProject: Project? = nil
    @Published var isPlanLoading: Bool = false
    @Published var planResult: PlanIssuesResult? = nil

    // Discuss
    @Published var discussSelectedProject: Project? = nil
    @Published var discussQuestion: String = ""
    @Published var isDiscussLoading: Bool = false
    @Published var discussResult: DiscussResult? = nil

    // Design
    @Published var designSelectedProject: Project? = nil
    @Published var designFigmaUrl: String = ""
    @Published var isDesignLoading: Bool = false
    @Published var designResult: DesignResult? = nil

    // Shell
    @Published var shellCommand: String = ""
    @Published var isShellLoading: Bool = false
    @Published var shellResult: ShellResult? = nil
    @Published var showDangerDialog: Bool = false
    @Published var pendingDangerousCommand: String? = nil

    // Common
    @Published var error: String? = nil

    private let viewModel: CommandCenterViewModel
    private var collectTask: Task<Void, Never>?

    init() {
        let container = IOSAppContainer.shared
        self.viewModel = container.createCommandCenterViewModel()
        startCollecting()
    }

    private func startCollecting() {
        collectTask?.cancel()
        collectTask = Task { @MainActor [weak self] in
            guard let self else { return }
            let collector = CommandCenterUiStateCollector { [weak self] state in
                self?.updateFromState(state)
            }
            try? await self.viewModel.uiState.collect(collector: collector)
        }
    }

    private func updateFromState(_ state: CommandCenterUiState) {
        self.activeTab = state.activeTab
        self.projects = state.projects as? [Project] ?? []
        self.isLoadingProjects = state.isLoadingProjects
        self.initName = state.initName
        self.initDescription = state.initDescription
        self.initVisibility = state.initVisibility
        self.isInitLoading = state.isInitLoading
        self.initResult = state.initResult
        self.planSelectedProject = state.planSelectedProject
        self.isPlanLoading = state.isPlanLoading
        self.planResult = state.planResult
        self.discussSelectedProject = state.discussSelectedProject
        self.discussQuestion = state.discussQuestion
        self.isDiscussLoading = state.isDiscussLoading
        self.discussResult = state.discussResult
        self.designSelectedProject = state.designSelectedProject
        self.designFigmaUrl = state.designFigmaUrl
        self.isDesignLoading = state.isDesignLoading
        self.designResult = state.designResult
        self.shellCommand = state.shellCommand
        self.isShellLoading = state.isShellLoading
        self.shellResult = state.shellResult
        self.showDangerDialog = state.showDangerDialog
        self.pendingDangerousCommand = state.pendingDangerousCommand
        self.error = state.error
    }

    func selectTab(_ tab: CommandTab) { viewModel.selectTab(tab: tab) }

    func updateInitName(_ name: String) { viewModel.updateInitName(name: name) }
    func updateInitDescription(_ desc: String) { viewModel.updateInitDescription(desc: desc) }
    func updateInitVisibility(_ vis: ProjectVisibility) { viewModel.updateInitVisibility(vis: vis) }
    func executeInit() { viewModel.executeInit() }

    func selectPlanProject(_ project: Project) { viewModel.selectPlanProject(project: project) }
    func executePlan() { viewModel.executePlan() }

    func selectDiscussProject(_ project: Project) { viewModel.selectDiscussProject(project: project) }
    func updateDiscussQuestion(_ q: String) { viewModel.updateDiscussQuestion(q: q) }
    func executeDiscuss() { viewModel.executeDiscuss() }
    func convertSuggestedIssue(_ issue: PlannedIssue) { viewModel.convertSuggestedIssue(issue: issue) }

    func selectDesignProject(_ project: Project) { viewModel.selectDesignProject(project: project) }
    func updateDesignFigmaUrl(_ url: String) { viewModel.updateDesignFigmaUrl(url: url) }
    func executeDesign() { viewModel.executeDesign() }

    func updateShellCommand(_ cmd: String) { viewModel.updateShellCommand(cmd: cmd) }
    func executeShell() { viewModel.executeShell() }
    func confirmDangerousCommand() { viewModel.confirmDangerousCommand() }
    func cancelDangerousCommand() { viewModel.cancelDangerousCommand() }

    func clearError() { viewModel.clearError() }

    func onCleared() {
        collectTask?.cancel()
        viewModel.onCleared()
    }
}

private final class CommandCenterUiStateCollector: Kotlinx_coroutines_coreFlowCollector {
    let onValue: (CommandCenterUiState) -> Void

    init(onValue: @escaping (CommandCenterUiState) -> Void) {
        self.onValue = onValue
    }

    func emit(value: Any?, completionHandler: @escaping (Error?) -> Void) {
        if let state = value as? CommandCenterUiState {
            onValue(state)
        }
        completionHandler(nil)
    }
}
