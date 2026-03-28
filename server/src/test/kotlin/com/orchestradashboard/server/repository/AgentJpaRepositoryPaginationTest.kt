package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.AgentEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest

@DataJpaTest
class AgentJpaRepositoryPaginationTest {
    @Autowired
    lateinit var repository: AgentJpaRepository

    @BeforeEach
    fun seed() {
        repository.deleteAll()
        (1..10).forEach {
            repository.save(
                AgentEntity(
                    id = "r-$it",
                    name = "Runner $it",
                    type = "WORKER",
                    status = "RUNNING",
                    lastHeartbeat = 1000L + it,
                ),
            )
        }
        (1..5).forEach {
            repository.save(
                AgentEntity(
                    id = "i-$it",
                    name = "Idler $it",
                    type = "WORKER",
                    status = "IDLE",
                    lastHeartbeat = 2000L + it,
                ),
            )
        }
    }

    @Test
    fun `should return paginated results when page size exceeds total`() {
        val page = repository.findAll(PageRequest.of(0, 50))
        assertEquals(15, page.content.size)
        assertEquals(15L, page.totalElements)
        assertEquals(1, page.totalPages)
    }

    @Test
    fun `should return correct page slice`() {
        val page = repository.findAll(PageRequest.of(1, 5))
        assertEquals(5, page.content.size)
        assertEquals(15L, page.totalElements)
        assertEquals(3, page.totalPages)
    }

    @Test
    fun `should return filtered paginated results by status`() {
        val page = repository.findByStatus("RUNNING", PageRequest.of(0, 5))
        assertEquals(5, page.content.size)
        assertEquals(10L, page.totalElements)
    }

    @Test
    fun `should return empty page for out-of-range page number`() {
        val page = repository.findAll(PageRequest.of(100, 10))
        assertTrue(page.content.isEmpty())
        assertEquals(15L, page.totalElements)
    }
}
