import SwiftUI
import Shared

/// Renders a set of parallel pipeline lanes stacked vertically with
/// dependency arrows drawn in a left gutter using SwiftUI Canvas.
struct ParallelPipelineView: View {
    let pipelines: [MonitoredPipeline]
    let dependencies: [PipelineDependency]

    var body: some View {
        ZStack(alignment: .topLeading) {
            // Lane stack
            VStack(alignment: .leading, spacing: 12) {
                ForEach(Array(pipelines.enumerated()), id: \.offset) { _, pipeline in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(pipeline.id)
                            .font(.caption)
                            .fontWeight(.medium)
                            .foregroundStyle(.blue)
                            .accessibilityIdentifier("lane_header_\(pipeline.id)")
                        StepTimelineView(steps: pipeline.steps)
                    }
                    .padding(.leading, 48) // leave room for arrow gutter
                }
            }

            // Arrow overlay in left gutter
            if !dependencies.isEmpty && !pipelines.isEmpty {
                GeometryReader { geometry in
                    let laneHeight = geometry.size.height / CGFloat(pipelines.count)
                    DependencyArrowCanvas(
                        dependencies: dependencies,
                        pipelineIds: pipelines.map { $0.id },
                        laneHeight: laneHeight
                    )
                    .frame(width: 44)
                    .accessibilityIdentifier("dependency_arrows")
                }
            }
        }
    }
}

// MARK: - Arrow Canvas

/// Draws curved dependency arrows between parallel pipeline lanes.
/// Geometry and color helpers are exposed as `static func` for unit testing.
struct DependencyArrowCanvas: View {
    let dependencies: [PipelineDependency]
    let pipelineIds: [String]
    let laneHeight: CGFloat

    var body: some View {
        Canvas { context, _ in
            for dep in dependencies {
                guard
                    let sourceIdx = pipelineIds.firstIndex(of: dep.sourceLaneId),
                    let targetIdx = pipelineIds.firstIndex(of: dep.targetLaneId),
                    sourceIdx != targetIdx
                else { continue }

                let (startY, endY) = DependencyArrowCanvas.calcArrowEndpoints(
                    sourceIdx: sourceIdx,
                    targetIdx: targetIdx,
                    laneHeight: laneHeight
                )
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

    /// Returns the Y center coordinates for source and target lanes.
    static func calcArrowEndpoints(
        sourceIdx: Int,
        targetIdx: Int,
        laneHeight: CGFloat
    ) -> (startY: CGFloat, endY: CGFloat) {
        let startY = CGFloat(sourceIdx) * laneHeight + laneHeight / 2
        let endY = CGFloat(targetIdx) * laneHeight + laneHeight / 2
        return (startY, endY)
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
        // Use trigonometry: down → base is above tip (negative Y offset); up → below
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
