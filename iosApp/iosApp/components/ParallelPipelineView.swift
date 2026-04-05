import SwiftUI
import Shared

/// Renders a set of parallel pipeline lanes stacked vertically with
/// dependency arrows drawn in a left gutter using SwiftUI Canvas.
struct ParallelPipelineView: View {
    let pipelines: [MonitoredPipeline]
    let dependencies: [PipelineDependency]

    @State private var laneHeights: [CGFloat] = []
    private let laneSpacing: CGFloat = 12

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            if !dependencies.isEmpty {
                DependencyLegendView()
            }

            ZStack(alignment: .topLeading) {
                VStack(alignment: .leading, spacing: laneSpacing) {
                    ForEach(Array(pipelines.enumerated()), id: \.offset) { index, pipeline in
                        laneContent(pipeline)
                            .background(
                                GeometryReader { geo in
                                    Color.clear.preference(
                                        key: LaneHeightPreferenceKey.self,
                                        value: [index: geo.size.height]
                                    )
                                }
                            )
                            .padding(.leading, dependencies.isEmpty ? 0 : 48)
                    }
                }
                .onPreferenceChange(LaneHeightPreferenceKey.self) { prefs in
                    var newHeights = Array(repeating: CGFloat(0), count: pipelines.count)
                    for (idx, h) in prefs {
                        if idx < newHeights.count {
                            newHeights[idx] = h
                        }
                    }
                    if newHeights != laneHeights {
                        laneHeights = newHeights
                    }
                }

                if !dependencies.isEmpty && !pipelines.isEmpty &&
                   laneHeights.count == pipelines.count {
                    DependencyArrowCanvas(
                        dependencies: dependencies,
                        pipelineIds: pipelines.map { $0.id },
                        laneHeights: laneHeights,
                        spacing: laneSpacing
                    )
                    .frame(width: 44)
                    .accessibilityIdentifier("dependency_arrows")
                }
            }
        }
    }

    // MARK: - Lane content

    @ViewBuilder
    private func laneContent(_ pipeline: MonitoredPipeline) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(pipeline.id)
                        .font(.caption)
                        .fontWeight(.medium)
                        .foregroundStyle(.blue)
                        .accessibilityIdentifier("lane_header_\(pipeline.id)")

                    if pipeline.issueNum > 0 {
                        Text("#\(pipeline.issueNum) \(pipeline.issueTitle)")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                            .lineLimit(1)
                    }
                }
                Spacer()
                let badgeColor = ParallelPipelineView.statusBadgeColor(for: pipeline.status)
                Text(ParallelPipelineView.statusBadgeLabel(for: pipeline.status))
                    .font(.caption2)
                    .fontWeight(.medium)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(badgeColor.opacity(0.15))
                    .foregroundStyle(badgeColor)
                    .clipShape(Capsule())
                    .accessibilityIdentifier("lane_status_\(pipeline.id)")
            }
            StepTimelineView(steps: pipeline.steps)
        }
    }

    // MARK: - Status badge helpers

    static func statusBadgeColor(for status: PipelineRunStatus) -> Color {
        switch status {
        case .running:   return .blue
        case .passed:    return .green
        case .failed:    return .red
        case .cancelled: return .orange
        case .queued:    return .gray
        default:         return .gray
        }
    }

    static func statusBadgeLabel(for status: PipelineRunStatus) -> String {
        switch status {
        case .running:   return "Running"
        case .passed:    return "Passed"
        case .failed:    return "Failed"
        case .cancelled: return "Cancelled"
        case .queued:    return "Queued"
        default:         return "Unknown"
        }
    }

    // MARK: - Dependency legend helper

    static func dependencyLegendLabel(for type: DependencyType) -> String {
        switch type {
        case .blocksStart:   return "Blocks Start"
        case .providesInput: return "Provides Input"
        default:             return "Dependency"
        }
    }
}

// MARK: - Dependency Legend

private struct DependencyLegendView: View {
    var body: some View {
        HStack(spacing: 16) {
            legendItem(color: .orange, label: "Blocks Start")
            legendItem(color: Color(red: 0.01, green: 0.66, blue: 0.96), label: "Provides Input")
        }
        .font(.caption2)
        .accessibilityIdentifier("dependency_legend")
    }

    private func legendItem(color: Color, label: String) -> some View {
        HStack(spacing: 4) {
            Circle().fill(color).frame(width: 8, height: 8)
            Text(label).foregroundStyle(.secondary)
        }
    }
}

// MARK: - Lane Height PreferenceKey

private struct LaneHeightPreferenceKey: PreferenceKey {
    static var defaultValue: [Int: CGFloat] = [:]

    static func reduce(value: inout [Int: CGFloat], nextValue: () -> [Int: CGFloat]) {
        value.merge(nextValue()) { _, new in new }
    }
}

// MARK: - Arrow Canvas

/// Draws curved dependency arrows between parallel pipeline lanes.
/// Geometry and color helpers are exposed as `static func` for unit testing.
struct DependencyArrowCanvas: View {
    let dependencies: [PipelineDependency]
    let pipelineIds: [String]
    var laneHeight: CGFloat = 0        // legacy equal-height mode
    var laneHeights: [CGFloat] = []    // dynamic-height mode
    var spacing: CGFloat = 12

    var body: some View {
        Canvas { context, _ in
            for dep in dependencies {
                guard
                    let sourceIdx = pipelineIds.firstIndex(of: dep.sourceLaneId),
                    let targetIdx = pipelineIds.firstIndex(of: dep.targetLaneId),
                    sourceIdx != targetIdx
                else { continue }

                let startY: CGFloat
                let endY: CGFloat

                if !laneHeights.isEmpty {
                    startY = DependencyArrowCanvas.calcLaneCenterY(
                        index: sourceIdx, laneHeights: laneHeights, spacing: spacing)
                    endY = DependencyArrowCanvas.calcLaneCenterY(
                        index: targetIdx, laneHeights: laneHeights, spacing: spacing)
                } else {
                    (startY, endY) = DependencyArrowCanvas.calcArrowEndpoints(
                        sourceIdx: sourceIdx, targetIdx: targetIdx, laneHeight: laneHeight)
                }

                let x: CGFloat = 22

                var curvePath = Path()
                curvePath.move(to: CGPoint(x: x, y: startY))
                curvePath.addCurve(
                    to: CGPoint(x: x, y: endY),
                    control1: CGPoint(x: x + 24, y: startY),
                    control2: CGPoint(x: x + 24, y: endY)
                )

                let color = DependencyArrowCanvas.arrowColor(for: dep.type)
                context.stroke(curvePath, with: .color(color), lineWidth: 2)

                let direction: ArrowDirection = targetIdx > sourceIdx ? .down : .up
                drawArrowhead(
                    context: context,
                    tip: CGPoint(x: x, y: endY),
                    direction: direction,
                    color: color
                )
            }
        }
    }

    // MARK: - Testable geometry helpers

    /// Returns the Y center coordinates for source and target lanes (equal-height mode).
    static func calcArrowEndpoints(
        sourceIdx: Int,
        targetIdx: Int,
        laneHeight: CGFloat
    ) -> (startY: CGFloat, endY: CGFloat) {
        let startY = CGFloat(sourceIdx) * laneHeight + laneHeight / 2
        let endY = CGFloat(targetIdx) * laneHeight + laneHeight / 2
        return (startY, endY)
    }

    /// Computes Y center of lane at `index` using actual measured lane heights.
    /// Accumulates heights[0..<index] + spacing for each, then adds half of heights[index].
    static func calcLaneCenterY(
        index: Int,
        laneHeights: [CGFloat],
        spacing: CGFloat
    ) -> CGFloat {
        guard index >= 0, index < laneHeights.count else { return 0 }
        var y: CGFloat = 0
        for i in 0..<index {
            y += laneHeights[i] + spacing
        }
        y += laneHeights[index] / 2
        return y
    }

    /// Maps a `DependencyType` to its arrow color.
    static func arrowColor(for type: DependencyType) -> Color {
        switch type {
        case .blocksStart:
            return .orange
        case .providesInput:
            return Color(red: 0.01, green: 0.66, blue: 0.96) // #03A9F4
        default:
            return .gray
        }
    }

    // MARK: - Private rendering helpers

    private func drawArrowhead(
        context: GraphicsContext,
        tip: CGPoint,
        direction: ArrowDirection,
        color: Color
    ) {
        let size: CGFloat = 7
        // down → base is above tip (negative Y offset); up → below
        let yOffset: CGFloat = direction == .down ? -size : size
        var path = Path()
        path.move(to: tip)
        path.addLine(to: CGPoint(x: tip.x - size / 2, y: tip.y + yOffset))
        path.addLine(to: CGPoint(x: tip.x + size / 2, y: tip.y + yOffset))
        path.closeSubpath()
        context.fill(path, with: .color(color))
    }

    enum ArrowDirection { case up, down }
}
