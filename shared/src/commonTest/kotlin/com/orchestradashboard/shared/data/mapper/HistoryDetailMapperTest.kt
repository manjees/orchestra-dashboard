package com.orchestradashboard.shared.data.mapper

import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryDetailDto
import com.orchestradashboard.shared.data.dto.orchestrator.PipelineHistoryPageDto
import com.orchestradashboard.shared.data.dto.orchestrator.StepHistoryDto
import com.orchestradashboard.shared.domain.model.PipelineRunStatus
import com.orchestradashboard.shared.domain.model.StepStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HistoryDetailMapperTest {
    private val mapper = HistoryDetailMapper()

    private fun createDetailDto(
        id: String = "h-1",
        status: String = "PASSED",
        completedAt: Long? = 1700003600L,
        steps: List<StepHistoryDto> = emptyList(),
    ) = PipelineHistoryDetailDto(
        id = id,
        projectName = "my-project",
        issueNum = 42,
        issueTitle = "Fix bug",
        mode = "solve",
        status = status,
        startedAt = 1700000000L,
        completedAt = completedAt,
        elapsedTotalSec = 3600.0,
        prUrl = "https://github.com/org/repo/pull/1",
        steps = steps,
    )

    @Test
    fun `toDomain maps all fields`() {
        val dto = createDetailDto()
        val domain = mapper.toDomain(dto)

        assertEquals("h-1", domain.id)
        assertEquals("my-project", domain.projectName)
        assertEquals(42, domain.issueNum)
        assertEquals("Fix bug", domain.issueTitle)
        assertEquals("solve", domain.mode)
        assertEquals(PipelineRunStatus.PASSED, domain.status)
        assertEquals(1700000000L, domain.startedAt)
        assertEquals(1700003600L, domain.completedAt)
        assertEquals(3600.0, domain.elapsedTotalSec)
        assertEquals("https://github.com/org/repo/pull/1", domain.prUrl)
    }

    @Test
    fun `toDomain handles null completedAt`() {
        val dto = createDetailDto(completedAt = null)
        val domain = mapper.toDomain(dto)

        assertNull(domain.completedAt)
    }

    @Test
    fun `toDomain maps step list with failDetail`() {
        val dto =
            createDetailDto(
                steps =
                    listOf(
                        StepHistoryDto("build", "PASSED", 120.0, null),
                        StepHistoryDto("test", "FAILED", 60.0, "Test timeout after 60s"),
                    ),
            )
        val domain = mapper.toDomain(dto)

        assertEquals(2, domain.steps.size)
        assertEquals("build", domain.steps[0].stepName)
        assertEquals(StepStatus.PASSED, domain.steps[0].status)
        assertEquals(120.0, domain.steps[0].elapsedSec)
        assertNull(domain.steps[0].failDetail)
        assertEquals("test", domain.steps[1].stepName)
        assertEquals(StepStatus.FAILED, domain.steps[1].status)
        assertEquals("Test timeout after 60s", domain.steps[1].failDetail)
    }

    @Test
    fun `toDomain maps unknown status to FAILED as fallback`() {
        val dto = createDetailDto(status = "UNKNOWN_STATUS")
        val domain = mapper.toDomain(dto)

        assertEquals(PipelineRunStatus.FAILED, domain.status)
    }

    @Test
    fun `toPagedDomain maps Spring Page fields correctly`() {
        val pageDto =
            PipelineHistoryPageDto(
                content = listOf(createDetailDto(id = "h-1"), createDetailDto(id = "h-2")),
                number = 1,
                size = 20,
                totalElements = 42L,
                totalPages = 3,
            )
        val paged = mapper.toPagedDomain(pageDto)

        assertEquals(2, paged.agents.size)
        assertEquals("h-1", paged.agents[0].id)
        assertEquals("h-2", paged.agents[1].id)
        assertEquals(1, paged.page)
        assertEquals(20, paged.pageSize)
        assertEquals(42L, paged.totalElements)
        assertEquals(3, paged.totalPages)
    }

    @Test
    fun `toPagedDomain PipelineResult has stringified completedAt`() {
        val pageDto =
            PipelineHistoryPageDto(
                content = listOf(createDetailDto(id = "h-1", completedAt = 1700003600L)),
            )
        val paged = mapper.toPagedDomain(pageDto)

        assertEquals("1700003600", paged.agents[0].completedAt)
    }

    @Test
    fun `toPagedDomain preserves null completedAt as null`() {
        val pageDto =
            PipelineHistoryPageDto(
                content = listOf(createDetailDto(id = "h-1", completedAt = null)),
            )
        val paged = mapper.toPagedDomain(pageDto)

        assertNull(paged.agents[0].completedAt)
    }

    @Test
    fun `toPagedDomain handles empty content`() {
        val pageDto = PipelineHistoryPageDto(content = emptyList(), totalElements = 0L, totalPages = 0)
        val paged = mapper.toPagedDomain(pageDto)

        assertEquals(0, paged.agents.size)
        assertEquals(0L, paged.totalElements)
    }
}
