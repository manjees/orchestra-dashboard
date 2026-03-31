import SwiftUI
import Shared

/// Displays RAM, CPU, Disk gauges and thermal pressure badge.
struct SystemHealthBarView: View {
    let status: SystemStatus

    var body: some View {
        HStack(spacing: 12) {
            GaugeView(label: "RAM", percent: status.ramPercent)
            GaugeView(label: "CPU", percent: status.cpuPercent)
            GaugeView(label: "Disk", percent: status.diskPercent)
            ThermalBadgeView(pressure: status.thermalPressure)
        }
        .padding()
        .background(RoundedRectangle(cornerRadius: 12).fill(.ultraThinMaterial))
    }
}

private struct GaugeView: View {
    let label: String
    let percent: Double

    private var color: Color {
        if percent > 90 { return .red }
        if percent > 70 { return .yellow }
        return .green
    }

    var body: some View {
        VStack(spacing: 4) {
            Text(label).font(.caption).foregroundStyle(.secondary)
            ProgressView(value: percent, total: 100)
                .tint(color)
            Text("\(Int(percent))%").font(.caption2).bold()
        }
    }
}

private struct ThermalBadgeView: View {
    let pressure: ThermalPressure

    private var color: Color {
        switch pressure {
        case .nominal: return .green
        case .moderate: return .yellow
        case .heavy: return .orange
        case .critical: return .red
        default: return .gray
        }
    }

    var body: some View {
        VStack(spacing: 4) {
            Text("Thermal").font(.caption).foregroundStyle(.secondary)
            Text(pressure.name)
                .font(.caption2).bold()
                .padding(.horizontal, 8).padding(.vertical, 2)
                .background(Capsule().fill(color.opacity(0.2)))
                .foregroundColor(color)
        }
    }
}
