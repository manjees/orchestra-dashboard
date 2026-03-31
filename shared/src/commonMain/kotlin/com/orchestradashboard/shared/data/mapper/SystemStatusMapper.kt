package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.domain.model.SystemStatus
import com.orchestradashboard.shared.domain.model.ThermalPressure

class SystemStatusMapper {
    fun toDomain(dto: SystemStatusDto): SystemStatus =
        SystemStatus(
            ramPercent = dto.ramPercent,
            cpuPercent = dto.cpuPercent,
            diskPercent = dto.diskPercent,
            thermalPressure = parseThermalPressure(dto.thermalPressure),
        )

    fun parseThermalPressure(value: String): ThermalPressure =
        ThermalPressure.entries.find { it.name.equals(value, ignoreCase = true) }
            ?: ThermalPressure.UNKNOWN
}
