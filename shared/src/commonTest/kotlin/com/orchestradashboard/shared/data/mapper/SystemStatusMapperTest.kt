package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OllamaModelDto
import com.orchestradashboard.shared.data.dto.orchestrator.OllamaStatusDto
import com.orchestradashboard.shared.data.dto.orchestrator.SystemStatusDto
import com.orchestradashboard.shared.data.dto.orchestrator.TmuxSessionDto
import com.orchestradashboard.shared.domain.model.ThermalPressure
import kotlin.test.Test
import kotlin.test.assertEquals

class SystemStatusMapperTest {
    private val mapper = SystemStatusMapper()

    private fun createDto(
        ramPercent: Double = 72.0,
        cpuPercent: Double = 45.0,
        diskPercent: Double = 58.0,
        thermalPressure: String = "nominal",
    ) = SystemStatusDto(
        ramTotalGb = 16.0,
        ramUsedGb = 11.52,
        ramPercent = ramPercent,
        cpuPercent = cpuPercent,
        thermalPressure = thermalPressure,
        diskTotalGb = 500.0,
        diskUsedGb = 290.0,
        diskPercent = diskPercent,
        ollama = OllamaStatusDto(online = true, models = listOf(OllamaModelDto("llama3", 4.0))),
        tmuxSessions = listOf(TmuxSessionDto("main", 1, "2024-01-01T00:00:00Z")),
    )

    @Test
    fun `maps SystemStatusDto to SystemStatus with all fields`() {
        val dto = createDto()
        val result = mapper.toDomain(dto)

        assertEquals(72.0, result.ramPercent)
        assertEquals(45.0, result.cpuPercent)
        assertEquals(58.0, result.diskPercent)
        assertEquals(ThermalPressure.NOMINAL, result.thermalPressure)
    }

    @Test
    fun `maps thermalPressure string nominal to ThermalPressure NOMINAL`() {
        val result = mapper.toDomain(createDto(thermalPressure = "nominal"))
        assertEquals(ThermalPressure.NOMINAL, result.thermalPressure)
    }

    @Test
    fun `maps thermalPressure string moderate to ThermalPressure MODERATE`() {
        val result = mapper.toDomain(createDto(thermalPressure = "moderate"))
        assertEquals(ThermalPressure.MODERATE, result.thermalPressure)
    }

    @Test
    fun `maps thermalPressure string heavy to ThermalPressure HEAVY`() {
        val result = mapper.toDomain(createDto(thermalPressure = "heavy"))
        assertEquals(ThermalPressure.HEAVY, result.thermalPressure)
    }

    @Test
    fun `maps thermalPressure string critical to ThermalPressure CRITICAL`() {
        val result = mapper.toDomain(createDto(thermalPressure = "critical"))
        assertEquals(ThermalPressure.CRITICAL, result.thermalPressure)
    }

    @Test
    fun `maps unknown thermalPressure string to ThermalPressure UNKNOWN`() {
        val result = mapper.toDomain(createDto(thermalPressure = "something_else"))
        assertEquals(ThermalPressure.UNKNOWN, result.thermalPressure)
    }

    @Test
    fun `maps case-insensitive thermalPressure`() {
        val result = mapper.toDomain(createDto(thermalPressure = "NOMINAL"))
        assertEquals(ThermalPressure.NOMINAL, result.thermalPressure)
    }
}
