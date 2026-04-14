import SwiftUI

enum SolveModeOption: String, CaseIterable, Identifiable {
    case express = "Express"
    case standard = "Standard"
    case full = "Full"
    case auto = "Auto"

    var id: String { rawValue }
}

struct SolveIssueItem: Identifiable {
    let id: Int
    let title: String
}

struct SolveDialogView: View {
    let issues: [SolveIssueItem]
    @Binding var selectedIssueIds: Set<Int>
    @Binding var selectedMode: SolveModeOption
    @Binding var isParallel: Bool
    let isSolving: Bool
    let solveError: String?
    let onSolve: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        NavigationView {
            Form {
                Section("Select Issues") {
                    ForEach(issues) { issue in
                        HStack {
                            Image(systemName: selectedIssueIds.contains(issue.id) ? "checkmark.circle.fill" : "circle")
                                .foregroundColor(selectedIssueIds.contains(issue.id) ? .accentColor : .secondary)
                            Text("#\(issue.id) \(issue.title)")
                        }
                        .contentShape(Rectangle())
                        .onTapGesture {
                            if selectedIssueIds.contains(issue.id) {
                                selectedIssueIds.remove(issue.id)
                            } else {
                                selectedIssueIds.insert(issue.id)
                            }
                        }
                    }
                }

                Section("Mode") {
                    Picker("Mode", selection: $selectedMode) {
                        ForEach(SolveModeOption.allCases) { mode in
                            Text(mode.rawValue).tag(mode)
                        }
                    }
                    .pickerStyle(.segmented)
                }

                if selectedIssueIds.count > 1 {
                    Section {
                        Toggle("Run in Parallel", isOn: $isParallel)
                    }
                }

                if let error = solveError {
                    Section {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }
            }
            .navigationTitle("Solve Issues")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel", action: onDismiss)
                }
                ToolbarItem(placement: .confirmationAction) {
                    if isSolving {
                        ProgressView()
                    } else {
                        Button("Solve", action: onSolve)
                            .disabled(selectedIssueIds.isEmpty)
                    }
                }
            }
        }
    }
}

#Preview {
    @State var selected: Set<Int> = [1]
    @State var mode: SolveModeOption = .auto
    @State var parallel = false
    return SolveDialogView(
        issues: [
            SolveIssueItem(id: 1, title: "Fix auth bug"),
            SolveIssueItem(id: 2, title: "Add dark mode"),
        ],
        selectedIssueIds: $selected,
        selectedMode: $mode,
        isParallel: $parallel,
        isSolving: false,
        solveError: nil,
        onSolve: {},
        onDismiss: {}
    )
}
