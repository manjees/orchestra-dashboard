package com.orchestradashboard.server.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PipelineRunMapperTest {
    private val mapper = PipelineRunMapper()

    @Test
    fun `toResponse maps all entity fields correctly`() {
        val stepsJson = """[{"name":"Build","status":"PASSED","detail":"OK","elapsed_ms":1200}]"""
        val entity =
            PipelineRunEntity(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "CI Pipeline",
                status = "RUNNING",
                steps = stepsJson,
                startedAt = 1700000000L,
                finishedAt = 1700001000L,
                triggerInfo = "manual",
            )

        val response = mapper.toResponse(entity)

        assertEquals("run-1", response.id)
        assertEquals("agent-1", response.agentId)
        assertEquals("CI Pipeline", response.pipelineName)
        assertEquals("RUNNING", response.status)
        assertEquals(1, response.steps.size)
        assertEquals("Build", response.steps[0].name)
        assertEquals("PASSED", response.steps[0].status)
        assertEquals("OK", response.steps[0].detail)
        assertEquals(1200L, response.steps[0].elapsedMs)
        assertEquals(1700000000L, response.startedAt)
        assertEquals(1700001000L, response.finishedAt)
        assertEquals("manual", response.triggerInfo)
    }

    @Test
    fun `toResponse handles empty steps JSON`() {
        val entity =
            PipelineRunEntity(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "Pipeline",
                status = "QUEUED",
                steps = "[]",
                startedAt = 100L,
            )

        val response = mapper.toResponse(entity)

        assertTrue(response.steps.isEmpty())
    }

    @Test
    fun `toResponse handles null finishedAt`() {
        val entity =
            PipelineRunEntity(
                id = "run-1",
                agentId = "agent-1",
                pipelineName = "Pipeline",
                status = "RUNNING",
                steps = "[]",
                startedAt = 100L,
                finishedAt = null,
            )

        val response = mapper.toResponse(entity)

        assertNull(response.finishedAt)
    }

    @Test
    fun `toResponseList maps list of entities`() {
        val entities =
            listOf(
                PipelineRunEntity(id = "r1", agentId = "a1", pipelineName = "P1", startedAt = 100L),
                PipelineRunEntity(id = "r2", agentId = "a2", pipelineName = "P2", startedAt = 200L),
            )

        val responses = mapper.toResponseList(entities)

        assertEquals(2, responses.size)
        assertEquals("r1", responses[0].id)
        assertEquals("r2", responses[1].id)
    }

    @Test
    fun `serializeSteps round-trips through deserializeSteps`() {
        val steps =
            listOf(
                PipelineStepRequest(name = "Build", status = "PASSED", detail = "OK", elapsedMs = 500L),
                PipelineStepRequest(name = "Test", status = "RUNNING", detail = "", elapsedMs = 0L),
            )

        val json = mapper.serializeSteps(steps)
        val result = mapper.deserializeSteps(json)

        assertEquals(2, result.size)
        assertEquals("Build", result[0].name)
        assertEquals("PASSED", result[0].status)
        assertEquals("Test", result[1].name)
    }

    @Test
    fun `deserializeSteps handles malformed JSON gracefully`() {
        val result = mapper.deserializeSteps("{not valid json")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `deserializeSteps handles blank string`() {
        val result = mapper.deserializeSteps("")

        assertTrue(result.isEmpty())
    }
}
