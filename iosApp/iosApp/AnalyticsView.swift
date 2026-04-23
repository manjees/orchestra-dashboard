import SwiftUI
import Shared

struct AnalyticsView: View {
    let project: String
    @StateObject private var viewModel: IOSAnalyticsViewModel

    init(project: String) {
        self.project = project
        _viewModel = StateObject(wrappedValue: IOSAnalyticsViewModel(project: project))
    }

    var body: some View {
        VStack(spacing: 0) {
            if let error = viewModel.error {
                errorBanner(error)
            }
            periodFilterBar

            if viewModel.isLoading && !AnalyticsView.hasAnyData(
                summary: viewModel.summary,
                trends: viewModel.durationTrends,
                failures: viewModel.stepFailures
            ) {
                Spacer()
                ProgressView()
                Spacer()
            } else if !AnalyticsView.hasAnyData(
                summary: viewModel.summary,
                trends: viewModel.durationTrends,
                failures: viewModel.stepFailures
            ) {
                emptyState
            } else {
                ScrollView {
                    VStack(spacing: 16) {
                        SuccessRateChartView(summary: viewModel.summary)
                        DurationTrendsChartView(trends: viewModel.durationTrends)
                        StepFailureHeatmapView(failures: viewModel.stepFailures)
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Analytics")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(action: { viewModel.refresh() }) {
                    Image(systemName: "arrow.clockwise")
                }
            }
        }
        .onDisappear { viewModel.onCleared() }
    }

    private func errorBanner(_ message: String) -> some View {
        HStack {
            Text(message).foregroundColor(.red)
            Spacer()
            Button(action: { viewModel.clearError() }) {
                Image(systemName: "xmark.circle.fill")
            }
        }
        .padding()
        .background(Color.red.opacity(0.1))
    }

    private var periodFilterBar: some View {
        HStack(spacing: 8) {
            periodChip(.week)
            periodChip(.month)
            periodChip(.all)
        }
        .padding(.horizontal)
    }

    private func periodChip(_ period: PeriodFilter) -> some View {
        Button(action: { viewModel.selectPeriod(period) }) {
            Text(AnalyticsView.periodLabel(period))
                .font(.caption)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(viewModel.selectedPeriod == period ? Color.accentColor : Color.gray.opacity(0.2))
                .foregroundColor(viewModel.selectedPeriod == period ? .white : .primary)
                .cornerRadius(16)
        }
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Spacer()
            Image(systemName: "chart.bar.xaxis")
                .font(.system(size: 48))
                .foregroundColor(.secondary)
            Text("No analytics data")
                .font(.headline)
                .foregroundColor(.secondary)
            Text("Run some pipelines to see analytics")
                .font(.caption)
                .foregroundColor(.secondary)
            Spacer()
        }
    }

    static func periodLabel(_ filter: PeriodFilter) -> String {
        switch filter {
        case .week: return "Week"
        case .month: return "Month"
        case .all: return "All"
        default: return filter.label
        }
    }

    static func hasAnyData(summary: PipelineAnalytics?, trends: [DurationTrend], failures: [StepFailureRate]) -> Bool {
        return summary != nil || !trends.isEmpty || !failures.isEmpty
    }
}

#Preview {
    NavigationView {
        AnalyticsView(project: "default")
    }
}
