package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.DesignResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.DiscussResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.InitProjectResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlannedIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.PlanIssuesResponseDto
import com.orchestradashboard.shared.data.dto.orchestrator.ShellResponseDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CommandMapperTest {
    private val mapper = CommandMapper()

    @Test
    fun `mapInitResponse converts InitProjectResponseDto to CommandResult`() {
        val dto = InitProjectResponseDto(success = true, message = "Created", pipelineId = "p-42")

        val result = mapper.mapInitResponse(dto)

        assertTrue(result.success)
        assertEquals("Created", result.message)
        assertEquals("p-42", result.pipelineId)
    }

    @Test
    fun `mapInitResponse with null pipelineId maps to null pipelineId`() {
        val dto = InitProjectResponseDto(success = false, message = "Failed", pipelineId = null)

        val result = mapper.mapInitResponse(dto)

        assertNull(result.pipelineId)
    }

    @Test
    fun `mapPlanResponse converts PlanIssuesResponseDto to PlanIssuesResult with issue list`() {
        val dto = PlanIssuesResponseDto(
            issues = listOf(
                PlannedIssueDto(title = "Issue 1", body = "Body 1", labels = listOf("bug")),
                PlannedIssueDto(title = "Issue 2", body = "Body 2", labels = listOf("feature")),
            ),
        )

        val result = mapper.mapPlanResponse(dto)

        assertEquals(2, result.issues.size)
        assertEquals("Issue 1", result.issues[0].title)
        assertEquals("Body 1", result.issues[0].body)
        assertEquals(listOf("bug"), result.issues[0].labels)
    }

    @Test
    fun `mapDiscussResponse converts DiscussResponseDto to DiscussResult`() {
        val dto = DiscussResponseDto(
            answer = "Here is the answer",
            suggestedIssues = listOf(PlannedIssueDto("suggestion", "body", emptyList())),
        )

        val result = mapper.mapDiscussResponse(dto)

        assertEquals("Here is the answer", result.answer)
        assertEquals(1, result.suggestedIssues.size)
        assertEquals("suggestion", result.suggestedIssues[0].title)
    }

    @Test
    fun `mapDesignResponse converts DesignResponseDto to DesignResult`() {
        val dto = DesignResponseDto(
            spec = "Component layout description",
            suggestedIssues = emptyList(),
        )

        val result = mapper.mapDesignResponse(dto)

        assertEquals("Component layout description", result.spec)
        assertTrue(result.suggestedIssues.isEmpty())
    }

    @Test
    fun `mapShellResponse converts ShellResponseDto to ShellResult with exit code`() {
        val dto = ShellResponseDto(output = "Hello World", exitCode = 0)

        val result = mapper.mapShellResponse(dto)

        assertEquals("Hello World", result.output)
        assertEquals(0, result.exitCode)
    }

    @Test
    fun `mapShellResponse maps non-zero exit code`() {
        val dto = ShellResponseDto(output = "Error: command not found", exitCode = 127)

        val result = mapper.mapShellResponse(dto)

        assertEquals(127, result.exitCode)
    }
}
