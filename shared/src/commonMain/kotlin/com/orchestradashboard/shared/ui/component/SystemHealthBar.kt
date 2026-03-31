package com.orchestradashboard.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.orchestradashboard.shared.domain.model.SystemStatus
import com.orchestradashboard.shared.domain.model.ThermalPressure

@Composable
fun SystemHealthBar(
    systemStatus: SystemStatus?,
    modifier: Modifier = Modifier,
) {
    if (systemStatus == null) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GaugeItem("RAM", systemStatus.ramPercent, Modifier.weight(1f))
            GaugeItem("CPU", systemStatus.cpuPercent, Modifier.weight(1f))
            GaugeItem("Disk", systemStatus.diskPercent, Modifier.weight(1f))
            ThermalBadge(systemStatus.thermalPressure, Modifier.weight(1f))
        }
    }
}

@Composable
private fun GaugeItem(
    label: String,
    percent: Double,
    modifier: Modifier = Modifier,
) {
    val color = gaugeColor(percent)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        LinearProgressIndicator(
            progress = { (percent / 100.0).toFloat() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            color = color,
        )
        Text("${percent.toInt()}%", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ThermalBadge(
    pressure: ThermalPressure,
    modifier: Modifier = Modifier,
) {
    val color = thermalColor(pressure)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Thermal", style = MaterialTheme.typography.labelSmall)
        Surface(
            shape = MaterialTheme.shapes.small,
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            Text(
                text = pressure.name.lowercase().replaceFirstChar { it.uppercase() },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                color = color,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private fun gaugeColor(percent: Double): Color =
    when {
        percent > 90.0 -> Color(0xFFE53935)
        percent > 70.0 -> Color(0xFFFDD835)
        else -> Color(0xFF43A047)
    }

private fun thermalColor(pressure: ThermalPressure): Color =
    when (pressure) {
        ThermalPressure.NOMINAL -> Color(0xFF43A047)
        ThermalPressure.MODERATE -> Color(0xFFFDD835)
        ThermalPressure.HEAVY -> Color(0xFFFB8C00)
        ThermalPressure.CRITICAL -> Color(0xFFE53935)
        ThermalPressure.UNKNOWN -> Color(0xFF9E9E9E)
    }
