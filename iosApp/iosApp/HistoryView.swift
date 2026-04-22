import SwiftUI
import Shared

/// Pipeline execution history screen for iOS.
/// Displays searchable, filterable, paginated list of completed pipeline runs.
struct HistoryView: View {
    @StateObject private var viewModel = IOSHistoryViewModel()

    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                if let error = viewModel.error {
                    HStack {
                        Text(error).foregroundColor(.red)
                        Spacer()
                        Button(action: { viewModel.clearError() }) {
                            Image(systemName: "xmark.circle.fill")
                        }
                    }
                    .padding()
                    .background(Color.red.opacity(0.1))
                }

                HStack {
                    Image(systemName: "magnifyingglass")
                    TextField(
                        "Search by issue title",
                        text: Binding(
                            get: { viewModel.searchQuery },
                            set: { viewModel.updateSearchQuery($0) }
                        )
                    )
                }
                .padding()

                statusFilterBar
                timeRangeBar

                if viewModel.isLoading && viewModel.historyItems.isEmpty {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if viewModel.historyItems.isEmpty {
                    Spacer()
                    Text("No pipeline history.")
                        .foregroundColor(.secondary)
                    Spacer()
                } else {
                    historyList
                }
            }
            .navigationTitle("History")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: { viewModel.refresh() }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .sheet(isPresented: Binding(
                get: { viewModel.showDetail },
                set: { newValue in if !newValue { viewModel.clearSelection() } }
            )) {
                historyDetailSheet
            }
        }
        .onDisappear { viewModel.onCleared() }
    }

    private var statusFilterBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                statusChip(title: "All", isSelected: viewModel.selectedStatus == nil) {
                    viewModel.applyStatus(nil)
                }
                statusChip(
                    title: "PASSED",
                    isSelected: viewModel.selectedStatus == .passed
                ) {
                    viewModel.applyStatus(.passed)
                }
                statusChip(
                    title: "FAILED",
                    isSelected: viewModel.selectedStatus == .failed
                ) {
                    viewModel.applyStatus(.failed)
                }
                statusChip(
                    title: "CANCELLED",
                    isSelected: viewModel.selectedStatus == .cancelled
                ) {
                    viewModel.applyStatus(.cancelled)
                }
            }
            .padding(.horizontal)
        }
    }

    private var timeRangeBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                statusChip(
                    title: "Any time",
                    isSelected: viewModel.selectedTimeRange == nil
                ) { viewModel.selectTimeRange(nil) }
                statusChip(
                    title: "24h",
                    isSelected: viewModel.selectedTimeRange == .last24Hours
                ) { viewModel.selectTimeRange(.last24Hours) }
                statusChip(
                    title: "7d",
                    isSelected: viewModel.selectedTimeRange == .last7Days
                ) { viewModel.selectTimeRange(.last7Days) }
                statusChip(
                    title: "30d",
                    isSelected: viewModel.selectedTimeRange == .last30Days
                ) { viewModel.selectTimeRange(.last30Days) }
            }
            .padding(.horizontal)
        }
    }

    private func statusChip(title: String, isSelected: Bool, onTap: @escaping () -> Void) -> some View {
        Button(action: onTap) {
            Text(title)
                .font(.caption)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? Color.accentColor : Color.gray.opacity(0.2))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(16)
        }
    }

    private var historyList: some View {
        List {
            ForEach(viewModel.historyItems, id: \.id) { result in
                Button(action: { viewModel.selectHistory(result.id) }) {
                    historyRow(result)
                }
            }
            if viewModel.isLoadingMore {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
            }
            if viewModel.hasNextPage {
                Color.clear
                    .frame(height: 1)
                    .onAppear { viewModel.loadNextPage() }
            }
        }
        .listStyle(.plain)
    }

    private func historyRow(_ result: PipelineResult) -> some View {
        HStack {
            Image(
                systemName: result.status == .passed
                    ? "checkmark.circle.fill"
                    : "xmark.circle.fill"
            )
            .foregroundColor(result.status == .passed ? .green : .red)
            VStack(alignment: .leading) {
                Text("\(result.projectName) #\(result.issueNum)")
                    .font(.body)
                Text(result.status.name)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            Spacer()
            Text("\(Int(result.elapsedTotalSec))s")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }

    @ViewBuilder
    private var historyDetailSheet: some View {
        if viewModel.isLoadingDetail {
            ProgressView()
        } else if let errorText = viewModel.detailError {
            VStack {
                Text("Failed to load detail").font(.headline)
                Text(errorText).font(.caption).foregroundColor(.red)
                Button("Close") { viewModel.clearSelection() }
            }
            .padding()
        } else if let detail = viewModel.historyDetail {
            VStack(alignment: .leading, spacing: 12) {
                HStack {
                    VStack(alignment: .leading) {
                        Text("\(detail.projectName) #\(detail.issueNum)")
                            .font(.title3)
                        Text(detail.issueTitle)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    Spacer()
                    Button(action: { viewModel.clearSelection() }) {
                        Image(systemName: "xmark.circle.fill")
                    }
                }
                Text("Status: \(detail.status.name)  ·  Mode: \(detail.mode)")
                    .font(.caption)
                Text("Duration: \(Int(detail.elapsedTotalSec))s")
                    .font(.caption)
                    .foregroundColor(.secondary)
                if let prUrl = detail.prUrl {
                    Text("PR: \(prUrl)")
                        .font(.caption2)
                        .foregroundColor(.accentColor)
                }
                Divider()
                Text("Steps").font(.headline)
                List {
                    ForEach(detail.steps, id: \.stepName) { step in
                        VStack(alignment: .leading) {
                            HStack {
                                Circle()
                                    .fill(stepColor(step.status))
                                    .frame(width: 10, height: 10)
                                Text(step.stepName)
                                Spacer()
                                Text("\(Int(step.elapsedSec))s")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            if let detail = step.failDetail {
                                Text(detail)
                                    .font(.caption2)
                                    .foregroundColor(.red)
                            }
                        }
                    }
                }
            }
            .padding()
        }
    }

    private func stepColor(_ status: StepStatus) -> Color {
        switch status {
        case .pending: return .gray
        case .running: return .blue
        case .passed: return .green
        case .failed: return .red
        case .skipped: return .orange
        default: return .gray
        }
    }
}

#Preview {
    HistoryView()
}
