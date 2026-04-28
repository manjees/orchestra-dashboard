import SwiftUI
import Shared

/// Panel that displays streaming logs for the currently selected pipeline step.
struct LogStreamPanelView: View {
    @ObservedObject var viewModel: IOSLogStreamViewModel

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            header

            content
        }
        .padding(8)
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }

    private var header: some View {
        HStack {
            Text(headerTitle)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(.secondary)
            Spacer()
            Button(action: { viewModel.stopStream() }) {
                Image(systemName: "xmark.circle.fill")
                    .foregroundStyle(.secondary)
            }
            .accessibilityLabel("Stop streaming")
        }
        .padding(.horizontal, 4)
    }

    private var headerTitle: String {
        if let stepId = viewModel.selectedStepId {
            return "Logs: \(stepId)"
        }
        return "Logs"
    }

    @ViewBuilder
    private var content: some View {
        if viewModel.streamState is LogStreamStateLoading {
            loadingView
        } else if let error = viewModel.streamState as? LogStreamStateError {
            errorView(message: error.message)
        } else {
            logListView
        }
    }

    private var loadingView: some View {
        HStack(spacing: 8) {
            ProgressView()
            Text("Connecting...")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(8)
    }

    private func errorView(message: String) -> some View {
        Text("Stream error: \(message)")
            .font(.caption)
            .foregroundStyle(.red)
            .padding(8)
    }

    @ViewBuilder
    private var logListView: some View {
        if viewModel.logs.isEmpty {
            Text("No logs yet")
                .font(.caption)
                .foregroundStyle(.secondary)
                .padding(8)
        } else {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 2) {
                        ForEach(Array(viewModel.logs.enumerated()), id: \.offset) { index, entry in
                            Text(LogStreamPanelView.formatLogEntry(entry))
                                .font(.system(.caption, design: .monospaced))
                                .foregroundStyle(LogStreamPanelView.logLevelColor(entry.level))
                                .id(index)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                    }
                    .padding(.horizontal, 4)
                }
                .frame(maxHeight: 300)
                .onChange(of: viewModel.logs.count) { _, newCount in
                    if newCount > 0 {
                        withAnimation {
                            proxy.scrollTo(newCount - 1, anchor: .bottom)
                        }
                    }
                }
            }
        }
    }

    // MARK: - Formatting helpers (mirror Kotlin `formatLogEntry` / `logLevelColor`)

    static func formatLogEntry(_ entry: LogEntry) -> String {
        let timePart = extractHms(from: entry.timestamp)
        let levelMarker: String
        switch entry.level {
        case .info: levelMarker = ""
        case .warn: levelMarker = " WARN"
        case .error: levelMarker = " ERROR"
        case .debug: levelMarker = " DEBUG"
        default: levelMarker = ""
        }

        let prefix: String
        if timePart.isEmpty && levelMarker.isEmpty {
            prefix = ""
        } else if timePart.isEmpty {
            prefix = "[\(levelMarker.trimmingCharacters(in: .whitespaces))] "
        } else {
            prefix = "[\(timePart)\(levelMarker)] "
        }
        return "\(prefix)\(entry.message)"
    }

    static func logLevelColor(_ level: LogLevel) -> Color {
        switch level {
        case .info: return .primary
        case .warn: return Color(red: 0.88, green: 0.55, blue: 0.06)
        case .error: return Color(red: 0.83, green: 0.18, blue: 0.18)
        case .debug: return .gray
        default: return .primary
        }
    }

    private static func extractHms(from timestamp: String) -> String {
        guard !timestamp.isEmpty else { return "" }
        let timeSection: String
        if let tRange = timestamp.range(of: "T") {
            timeSection = String(timestamp[tRange.upperBound...])
        } else {
            timeSection = timestamp
        }
        let candidate = String(timeSection.prefix(8))
        guard candidate.count == 8 else { return "" }
        let chars = Array(candidate)
        if chars[2] == ":" && chars[5] == ":" {
            return candidate
        }
        return ""
    }
}
