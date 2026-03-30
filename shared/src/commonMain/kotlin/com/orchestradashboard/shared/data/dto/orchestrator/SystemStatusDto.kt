package com.orchestradashboard.shared.data.dto.orchestrator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SystemStatusDto(
    @SerialName("ram_total_gb") val ramTotalGb: Double,
    @SerialName("ram_used_gb") val ramUsedGb: Double,
    @SerialName("ram_percent") val ramPercent: Double,
    @SerialName("cpu_percent") val cpuPercent: Double,
    @SerialName("thermal_pressure") val thermalPressure: String,
    @SerialName("disk_total_gb") val diskTotalGb: Double,
    @SerialName("disk_used_gb") val diskUsedGb: Double,
    @SerialName("disk_percent") val diskPercent: Double,
    val ollama: OllamaStatusDto,
    @SerialName("tmux_sessions") val tmuxSessions: List<TmuxSessionDto>,
)

@Serializable
data class OllamaStatusDto(
    val online: Boolean,
    val models: List<OllamaModelDto>,
)

@Serializable
data class OllamaModelDto(
    val name: String,
    @SerialName("size_gb") val sizeGb: Double,
)

@Serializable
data class TmuxSessionDto(
    val name: String,
    val windows: Int,
    val created: String,
)
