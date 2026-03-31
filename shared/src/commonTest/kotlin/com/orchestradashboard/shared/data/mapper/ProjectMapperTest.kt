package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.OrchestratorIssueDto
import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectMapperTest {
    private val mapper = ProjectMapper()

    @Test
    fun `toDomain maps ProjectDto to Project with all fields`() {
        val dto =
            ProjectDto(
                name = "my-project",
                path = "/home/user/projects/my-project",
                ciCommands = listOf("./gradlew build"),
                openIssuesCount = 5,
                recentSolves = 3,
            )

        val result = mapper.toDomain(dto)

        assertEquals("my-project", result.name)
        assertEquals("/home/user/projects/my-project", result.path)
        assertEquals(5, result.openIssuesCount)
        assertEquals(3, result.recentSolves)
    }

    @Test
    fun `toDomain maps OrchestratorIssueDto to Issue with all fields`() {
        val dto =
            OrchestratorIssueDto(
                number = 42,
                title = "Fix login bug",
                labels = listOf("bug", "high-priority"),
                state = "open",
                createdAt = "2024-01-15T10:30:00Z",
            )

        val result = mapper.toDomain(dto)

        assertEquals(42, result.number)
        assertEquals("Fix login bug", result.title)
        assertEquals(listOf("bug", "high-priority"), result.labels)
        assertEquals("open", result.state)
        assertEquals("2024-01-15T10:30:00Z", result.createdAt)
    }

    @Test
    fun `toDomain maps empty labels list correctly`() {
        val dto =
            OrchestratorIssueDto(
                number = 1,
                title = "No labels",
                labels = emptyList(),
                state = "open",
                createdAt = "2024-01-01T00:00:00Z",
            )

        val result = mapper.toDomain(dto)

        assertTrue(result.labels.isEmpty())
    }

    @Test
    fun `toDomain maps list of ProjectDto`() {
        val dtos =
            listOf(
                ProjectDto("proj-a", "/a", listOf("make"), 2, 1),
                ProjectDto("proj-b", "/b", listOf("gradle"), 0, 0),
            )

        val results = mapper.toDomain(dtos)

        assertEquals(2, results.size)
        assertEquals("proj-a", results[0].name)
        assertEquals("proj-b", results[1].name)
    }

    @Test
    fun `issuesToDomain maps list of OrchestratorIssueDto`() {
        val dtos =
            listOf(
                OrchestratorIssueDto(1, "Issue A", listOf("bug"), "open", "2024-01-01T00:00:00Z"),
                OrchestratorIssueDto(2, "Issue B", emptyList(), "closed", "2024-02-01T00:00:00Z"),
            )

        val results = mapper.issuesToDomain(dtos)

        assertEquals(2, results.size)
        assertEquals(1, results[0].number)
        assertEquals(2, results[1].number)
    }

    @Test
    fun `toDomain maps empty project list`() {
        val results = mapper.toDomain(emptyList())
        assertTrue(results.isEmpty())
    }

    @Test
    fun `issuesToDomain maps empty issue list`() {
        val dtos: List<OrchestratorIssueDto> = emptyList()
        val results = mapper.issuesToDomain(dtos)
        assertTrue(results.isEmpty())
    }
}
