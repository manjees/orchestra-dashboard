package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.ProjectDto
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectMapperTest {
    private val mapper = ProjectMapper()

    @Test
    fun `maps ProjectDto to Project domain model`() {
        val dto =
            ProjectDto(
                name = "my-project",
                path = "/home/user/my-project",
                ciCommands = listOf("pytest", "flake8"),
                openIssuesCount = 5,
                recentSolves = 3,
            )
        val result = mapper.toDomain(dto)
        assertEquals("my-project", result.name)
        assertEquals("/home/user/my-project", result.path)
        assertEquals(listOf("pytest", "flake8"), result.ciCommands)
        assertEquals(5, result.openIssuesCount)
        assertEquals(3, result.recentSolves)
    }

    @Test
    fun `maps list of ProjectDto to list of Project`() {
        val dtos =
            listOf(
                ProjectDto("a", "/a", emptyList(), 1, 0),
                ProjectDto("b", "/b", listOf("make"), 2, 1),
            )
        val result = mapper.toDomain(dtos)
        assertEquals(2, result.size)
        assertEquals("a", result[0].name)
        assertEquals("b", result[1].name)
    }
}
