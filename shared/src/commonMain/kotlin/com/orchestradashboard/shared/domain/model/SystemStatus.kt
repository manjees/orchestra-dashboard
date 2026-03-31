package com.orchestradashboard.shared.domain.model

data class SystemStatus(
    val ramPercent: Double,
    val cpuPercent: Double,
    val diskPercent: Double,
    val thermalPressure: ThermalPressure,
)
