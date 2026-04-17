package com.orchestradashboard.server.service

import com.orchestradashboard.server.model.PipelineHistoryEntity
import com.orchestradashboard.server.model.PipelineHistoryMapper
import com.orchestradashboard.server.model.PipelineStepHistoryEntity
import com.orchestradashboard.server.repository.PipelineHistoryJpaRepository
import com.orchestradashboard.server.repository.PipelineStepHistoryJpaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

class PipelineHistoryServiceTest {
    private val historyRepository: PipelineHistoryJpaRepository = mock()
    private val stepRepository: PipelineStepHistoryJpaRepository = mock()
    private val mapper = PipelineHistoryMapper()
    private val service = PipelineHistoryService(historyRepository, stepRepository, mapper)

    private val sampleEntity =
        PipelineHistoryEntity(
            id = "h-1",
            projectName = "my-project",
            issueNum = 42,
            issueTitle = "Fix bug",
            mode = "solve",
            status = "PASSED",
            startedAt = 1700000000L,
            completedAt = 1700003600L,
            elapsedTotalSec = 3600.0,
            prUrl = "https://github.com/org/repo/pull/1",
        )

    @Test
    fun `getHistory without filters returns all`() {
        val pageable = PageRequest.of(0, 20)
        whenever(historyRepository.findAll(pageable))
            .thenReturn(PageImpl(listOf(sampleEntity), pageable, 1))

        val result = service.getHistory(null, null, pageable)

        assertEquals(1, result.totalElements)
        assertEquals("h-1", result.content[0].id)
        assertEquals("my-project", result.content[0].projectName)
    }

    @Test
    fun `getHistory with project filter`() {
        val pageable = PageRequest.of(0, 20)
        whenever(historyRepository.findByProjectName("my-project", pageable))
            .thenReturn(PageImpl(listOf(sampleEntity), pageable, 1))

        val result = service.getHistory("my-project", null, pageable)

        assertEquals(1, result.totalElements)
    }

    @Test
    fun `getHistory with status filter`() {
        val pageable = PageRequest.of(0, 20)
        whenever(historyRepository.findByStatus("PASSED", pageable))
            .thenReturn(PageImpl(listOf(sampleEntity), pageable, 1))

        val result = service.getHistory(null, "PASSED", pageable)

        assertEquals(1, result.totalElements)
    }

    @Test
    fun `getHistory with project and status filters`() {
        val pageable = PageRequest.of(0, 20)
        whenever(historyRepository.findByProjectNameAndStatus("my-project", "PASSED", pageable))
            .thenReturn(PageImpl(listOf(sampleEntity), pageable, 1))

        val result = service.getHistory("my-project", "PASSED", pageable)

        assertEquals(1, result.totalElements)
    }

    @Test
    fun `getHistoryById returns response when found`() {
        whenever(historyRepository.findById("h-1")).thenReturn(Optional.of(sampleEntity))

        val result = service.getHistoryById("h-1")

        assertEquals("h-1", result.id)
        assertEquals("my-project", result.projectName)
    }

    @Test
    fun `getHistoryById throws when not found`() {
        whenever(historyRepository.findById("missing")).thenReturn(Optional.empty())

        assertThrows<NoSuchElementException> {
            service.getHistoryById("missing")
        }
    }

    @Test
    fun `saveHistory delegates to repository`() {
        whenever(historyRepository.save(any<PipelineHistoryEntity>())).thenReturn(sampleEntity)

        val result = service.saveHistory(sampleEntity)

        assertEquals("h-1", result.id)
        verify(historyRepository).save(sampleEntity)
    }

    @Test
    fun `saveStep delegates to repository`() {
        val stepEntity =
            PipelineStepHistoryEntity(
                id = "s-1",
                pipelineHistoryId = "h-1",
                stepName = "build",
                status = "PASSED",
                elapsedSec = 60.0,
            )
        whenever(stepRepository.save(any<PipelineStepHistoryEntity>())).thenReturn(stepEntity)

        val result = service.saveStep(stepEntity)

        assertEquals("s-1", result.id)
        verify(stepRepository).save(stepEntity)
    }

    @Test
    fun `findHistoryByProjectAndDateRange delegates to repository`() {
        whenever(historyRepository.findByProjectNameAndStartedAtBetween("my-project", 1000L, 2000L))
            .thenReturn(listOf(sampleEntity))

        val result = service.findHistoryByProjectAndDateRange("my-project", 1000L, 2000L)

        assertEquals(1, result.size)
        assertEquals("h-1", result[0].id)
    }
}
