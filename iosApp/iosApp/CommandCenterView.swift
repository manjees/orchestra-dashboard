import SwiftUI
import Shared

/// SwiftUI view for the Command Center screen.
/// Provides tabbed access to Init, Plan, Discuss, Design, and Shell commands.
struct CommandCenterView: View {
    @StateObject private var viewModel = IOSCommandCenterViewModel()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                if let error = viewModel.error {
                    HStack {
                        Text(error)
                            .font(.footnote)
                            .foregroundStyle(.red)
                        Spacer()
                        Button("Dismiss") { viewModel.clearError() }
                            .font(.footnote)
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                    .background(Color.red.opacity(0.1))
                }

                Picker("Tab", selection: Binding(
                    get: { viewModel.activeTab },
                    set: { viewModel.selectTab($0) }
                )) {
                    Text("Init").tag(CommandTab.iNIT)
                    Text("Plan").tag(CommandTab.pLAN)
                    Text("Discuss").tag(CommandTab.dISCUSS)
                    Text("Design").tag(CommandTab.dESIGN)
                    Text("Shell").tag(CommandTab.sHELL)
                }
                .pickerStyle(.segmented)
                .padding()

                ScrollView {
                    switch viewModel.activeTab {
                    case .iNIT:
                        InitTabView(viewModel: viewModel)
                    case .pLAN:
                        PlanTabView(viewModel: viewModel)
                    case .dISCUSS:
                        DiscussTabView(viewModel: viewModel)
                    case .dESIGN:
                        DesignTabView(viewModel: viewModel)
                    case .sHELL:
                        ShellTabView(viewModel: viewModel)
                    default:
                        EmptyView()
                    }
                }
            }
            .navigationTitle("Command Center")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Back") { dismiss() }
                }
            }
        }
        .onDisappear { viewModel.onCleared() }
        .alert("Dangerous Command", isPresented: $viewModel.showDangerDialog) {
            Button("Cancel", role: .cancel) { viewModel.cancelDangerousCommand() }
            Button("Execute", role: .destructive) { viewModel.confirmDangerousCommand() }
        } message: {
            Text("This command may be destructive: \(viewModel.pendingDangerousCommand ?? "")")
        }
    }
}

// MARK: - Init Tab

private struct InitTabView: View {
    @ObservedObject var viewModel: IOSCommandCenterViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("Project name", text: Binding(
                get: { viewModel.initName },
                set: { viewModel.updateInitName($0) }
            ))
            .textFieldStyle(.roundedBorder)

            TextField("Description", text: Binding(
                get: { viewModel.initDescription },
                set: { viewModel.updateInitDescription($0) }
            ))
            .textFieldStyle(.roundedBorder)

            Picker("Visibility", selection: Binding(
                get: { viewModel.initVisibility },
                set: { viewModel.updateInitVisibility($0) }
            )) {
                Text("Public").tag(ProjectVisibility.pUBLIC)
                Text("Private").tag(ProjectVisibility.pRIVATE)
            }
            .pickerStyle(.segmented)

            Button(action: { viewModel.executeInit() }) {
                if viewModel.isInitLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                } else {
                    Text("Initialize Project")
                        .frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(viewModel.initName.isEmpty || viewModel.isInitLoading)

            if let result = viewModel.initResult {
                VStack(alignment: .leading, spacing: 4) {
                    Label(result.success ? "Success" : "Failed", systemImage: result.success ? "checkmark.circle" : "xmark.circle")
                        .foregroundStyle(result.success ? .green : .red)
                    Text(result.message)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .padding()
                .background(Color.secondary.opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding()
    }
}

// MARK: - Plan Tab

private struct PlanTabView: View {
    @ObservedObject var viewModel: IOSCommandCenterViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            ProjectPicker(
                projects: viewModel.projects,
                selected: viewModel.planSelectedProject,
                onSelect: { viewModel.selectPlanProject($0) }
            )

            Button(action: { viewModel.executePlan() }) {
                if viewModel.isPlanLoading {
                    ProgressView().frame(maxWidth: .infinity)
                } else {
                    Text("Generate Plan").frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(viewModel.planSelectedProject == nil || viewModel.isPlanLoading)

            if let result = viewModel.planResult {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Planned Issues")
                        .font(.headline)
                    ForEach(result.issues, id: \.title) { issue in
                        PlannedIssueRow(issue: issue)
                    }
                }
            }
        }
        .padding()
    }
}

// MARK: - Discuss Tab

private struct DiscussTabView: View {
    @ObservedObject var viewModel: IOSCommandCenterViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            ProjectPicker(
                projects: viewModel.projects,
                selected: viewModel.discussSelectedProject,
                onSelect: { viewModel.selectDiscussProject($0) }
            )

            TextField("What should I implement first?", text: Binding(
                get: { viewModel.discussQuestion },
                set: { viewModel.updateDiscussQuestion($0) }
            ), axis: .vertical)
            .textFieldStyle(.roundedBorder)
            .lineLimit(3...)

            Button(action: { viewModel.executeDiscuss() }) {
                if viewModel.isDiscussLoading {
                    ProgressView().frame(maxWidth: .infinity)
                } else {
                    Text("Ask").frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(viewModel.discussSelectedProject == nil || viewModel.discussQuestion.isEmpty || viewModel.isDiscussLoading)

            if let result = viewModel.discussResult {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Answer")
                        .font(.caption)
                        .foregroundStyle(.blue)
                    Text(result.answer)
                        .font(.subheadline)

                    if !(result.suggestedIssues.isEmpty) {
                        Text("Suggested Issues")
                            .font(.caption)
                            .foregroundStyle(.blue)
                            .padding(.top, 4)
                        ForEach(result.suggestedIssues, id: \.title) { issue in
                            SuggestedIssueRow(issue: issue) {
                                viewModel.convertSuggestedIssue(issue)
                            }
                        }
                    }
                }
            }
        }
        .padding()
    }
}

// MARK: - Design Tab

private struct DesignTabView: View {
    @ObservedObject var viewModel: IOSCommandCenterViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            ProjectPicker(
                projects: viewModel.projects,
                selected: viewModel.designSelectedProject,
                onSelect: { viewModel.selectDesignProject($0) }
            )

            TextField("Figma URL", text: Binding(
                get: { viewModel.designFigmaUrl },
                set: { viewModel.updateDesignFigmaUrl($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .keyboardType(.URL)
            .autocorrectionDisabled()

            Button(action: { viewModel.executeDesign() }) {
                if viewModel.isDesignLoading {
                    ProgressView().frame(maxWidth: .infinity)
                } else {
                    Text("Generate Spec").frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(viewModel.designSelectedProject == nil || viewModel.designFigmaUrl.isEmpty || viewModel.isDesignLoading)

            if let result = viewModel.designResult {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Specification")
                        .font(.caption)
                        .foregroundStyle(.blue)
                    Text(result.spec)
                        .font(.subheadline)
                }
            }
        }
        .padding()
    }
}

// MARK: - Shell Tab

private struct ShellTabView: View {
    @ObservedObject var viewModel: IOSCommandCenterViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            TextField("Enter shell command", text: Binding(
                get: { viewModel.shellCommand },
                set: { viewModel.updateShellCommand($0) }
            ))
            .textFieldStyle(.roundedBorder)
            .autocorrectionDisabled()
            .autocapitalization(.none)

            Button(action: { viewModel.executeShell() }) {
                if viewModel.isShellLoading {
                    ProgressView().frame(maxWidth: .infinity)
                } else {
                    Text("Execute").frame(maxWidth: .infinity)
                }
            }
            .buttonStyle(.borderedProminent)
            .disabled(viewModel.shellCommand.isEmpty || viewModel.isShellLoading)

            if let result = viewModel.shellResult {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Exit code: \(result.exitCode)")
                        .font(.caption)
                        .foregroundStyle(result.exitCode == 0 ? .green : .red)
                    Text(result.output)
                        .font(.system(.footnote, design: .monospaced))
                }
                .padding()
                .background(Color.black.opacity(0.05))
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
        }
        .padding()
    }
}

// MARK: - Shared subviews

private struct ProjectPicker: View {
    let projects: [Project]
    let selected: Project?
    let onSelect: (Project) -> Void

    var body: some View {
        Menu {
            ForEach(projects, id: \.name) { project in
                Button(project.name) { onSelect(project) }
            }
        } label: {
            HStack {
                Text(selected?.name ?? "Select project")
                    .foregroundStyle(selected == nil ? .secondary : .primary)
                Spacer()
                Image(systemName: "chevron.up.chevron.down")
                    .foregroundStyle(.secondary)
            }
            .padding(8)
            .overlay(RoundedRectangle(cornerRadius: 6).stroke(Color.secondary.opacity(0.4)))
        }
    }
}

private struct PlannedIssueRow: View {
    let issue: PlannedIssue

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(issue.title)
                .font(.subheadline)
            Text(issue.body)
                .font(.caption)
                .foregroundStyle(.secondary)
                .lineLimit(2)
        }
        .padding(.vertical, 4)
    }
}

private struct SuggestedIssueRow: View {
    let issue: PlannedIssue
    let onConvertToIssue: () -> Void

    var body: some View {
        HStack {
            Text(issue.title)
                .font(.subheadline)
            Spacer()
            Button("Convert to Issue", action: onConvertToIssue)
                .font(.caption)
                .buttonStyle(.bordered)
        }
        .padding(.vertical, 4)
    }
}

#Preview {
    CommandCenterView()
}
