package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.SolveCommandResponseDto
import com.orchestradashboard.shared.domain.model.SolveMode
import com.orchestradashboard.shared.domain.model.SolveRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class SolveCommandMapperTest {
    private val mapper = SolveCommandMapper()

    @Test
    fun `maps SolveRequest to SolveCommandRequestDto correctly`() {
        val request = SolveRequest(
            projectName = "my-project",
            issueNumbers = listOf(1, 2, 3),
            mode = SolveMode.STANDARD,
            parallel = true,
        )
        val dto = mapper.toDto(request)
        assertEquals("my-project", dto.projectName)
        assertEquals(listOf(1, 2, 3), dto.issueNumbers)
        assertEquals("standard", dto.mode)
        assertEquals(true, dto.parallel)
    }

    @Test
    fun `maps SolveMode EXPRESS to express string`() {
        val request = SolveRequest("p", listOf(1), SolveMode.EXPRESS, false)
        val dto = mapper.toDto(request)
        assertEquals("express", dto.mode)
    }

    @Test
    fun `maps SolveMode STANDARD to standard string`() {
        val request = SolveRequest("p", listOf(1), SolveMode.STANDARD, false)
        val dto = mapper.toDto(request)
        assertEquals("standard", dto.mode)
    }

    @Test
    fun `maps SolveMode FULL to full string`() {
        val request = SolveRequest("p", listOf(1), SolveMode.FULL, false)
        val dto = mapper.toDto(request)
        assertEquals("full", dto.mode)
    }

    @Test
    fun `maps SolveMode AUTO to auto string`() {
        val request = SolveRequest("p", listOf(1), SolveMode.AUTO, false)
        val dto = mapper.toDto(request)
        assertEquals("auto", dto.mode)
    }

    @Test
    fun `maps parallel true when multiple issues selected`() {
        val request = SolveRequest("p", listOf(1, 2), SolveMode.AUTO, true)
        val dto = mapper.toDto(request)
        assertEquals(true, dto.parallel)
    }

    @Test
    fun `maps parallel false for single issue`() {
        val request = SolveRequest("p", listOf(1), SolveMode.AUTO, false)
        val dto = mapper.toDto(request)
        assertEquals(false, dto.parallel)
    }

    @Test
    fun `maps SolveResponseDto to SolveResponse correctly`() {
        val responseDto = SolveCommandResponseDto(pipelineId = "pipe-42", status = "started")
        val response = mapper.toDomain(responseDto)
        assertEquals("pipe-42", response.pipelineId)
        assertEquals("started", response.status)
    }
}
