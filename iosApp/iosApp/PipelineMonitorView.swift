import SwiftUI
import Shared

/// Pipeline Monitor screen — displays step timeline, live logs, and approval dialogs.
struct PipelineMonitorView: View {
    @StateObject private var viewModel: IOSPipelineMonitorViewModel

    init(pipelineId: String) {
        _viewModel = StateObject(wrappedValue: IOSPipelineMonitorViewModel(pipelineId: pipelineId))
    }

    var body: some View {
        VStack(spacing: 0) {
            if viewModel.isLoading {
                ProgressView("Loading pipeline...")
            } else {
                ScrollView {
                    VStack(spacing: 16) {
                        if viewModel.isParallelView {
                            ParallelPipelineView(
                                pipelines: viewModel.parallelPipelines,
                                dependencies: viewModel.dependencies
                            )
                        } else if let pipeline = viewModel.pipeline {
                            StepTimelineView(steps: pipeline.steps)
                        }

                        logPanel
                    }
                    .padding()
                }
            }
        }
        .navigationTitle(
            viewModel.pipeline.map { "\($0.projectName) #\($0.issueNum)" } ?? "Pipeline Monitor"
        )
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: { viewModel.refresh() }) {
                    Image(systemName: "arrow.clockwise")
                }
            }
        }
        .onAppear {
            viewModel.loadPipeline()
            viewModel.startObserving()
        }
        .onDisappear { viewModel.onCleared() }
        .alert("Error", isPresented: .constant(viewModel.error != nil)) {
            Button("OK") { viewModel.clearError() }
        } message: {
            Text(viewModel.error ?? "")
        }
        .sheet(isPresented: .constant(viewModel.pendingApproval != nil)) {
            if let approval = viewModel.pendingApproval {
                ApprovalDialogView(approval: approval, onDismiss: { viewModel.dismissApproval() })
            }
        }
    }

    private var logPanel: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Logs")
                .font(.headline)

            if viewModel.logLines.isEmpty {
                Text("Waiting for logs...")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            } else {
                ScrollView {
                    LazyVStack(alignment: .leading) {
                        ForEach(Array(viewModel.logLines.enumerated()), id: \.offset) { _, line in
                            Text(line)
                                .font(.system(.caption, design: .monospaced))
                        }
                    }
                }
                .frame(maxHeight: 300)
                .background(Color(.systemGray6))
                .cornerRadius(8)
            }
        }
    }
}
