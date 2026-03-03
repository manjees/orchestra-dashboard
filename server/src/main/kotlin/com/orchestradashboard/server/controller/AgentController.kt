package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.AgentResponse
import com.orchestradashboard.server.model.CreateAgentRequest
import com.orchestradashboard.server.model.UpdateAgentStatusRequest
import com.orchestradashboard.server.service.AgentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for AI agent management.
 * All endpoints are versioned under `/api/v1/agents`.
 */
@RestController
@RequestMapping("/api/v1/agents")
class AgentController(
    private val agentService: AgentService
) {

    /**
     * Returns all registered agents.
     *
     * @return 200 OK with a list of agent responses
     */
    @GetMapping
    fun getAgents(): ResponseEntity<List<AgentResponse>> {
        return ResponseEntity.ok(agentService.getAllAgents())
    }

    /**
     * Returns a specific agent by ID.
     *
     * @param id Unique agent identifier
     * @return 200 OK with agent details, or 404 if not found
     */
    @GetMapping("/{id}")
    fun getAgent(@PathVariable id: String): ResponseEntity<AgentResponse> {
        return agentService.getAgent(id)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    /**
     * Registers a new agent.
     *
     * @param request Agent registration payload
     * @return 201 Created with the registered agent, or 409 Conflict if ID is taken
     */
    @PostMapping
    fun registerAgent(@RequestBody request: CreateAgentRequest): ResponseEntity<AgentResponse> {
        return try {
            ResponseEntity.status(201).body(agentService.registerAgent(request))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(409).build()
        }
    }

    /**
     * Updates the status of an existing agent.
     *
     * @param id Unique agent identifier
     * @param request Status update payload
     * @return 200 OK with updated agent, or 404 if not found
     */
    @PatchMapping("/{id}/status")
    fun updateAgentStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateAgentStatusRequest
    ): ResponseEntity<AgentResponse> {
        return agentService.updateAgentStatus(id, request)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()
    }

    /**
     * Deregisters an agent from monitoring.
     *
     * @param id Unique agent identifier
     * @return 204 No Content on success, or 404 if not found
     */
    @DeleteMapping("/{id}")
    fun deregisterAgent(@PathVariable id: String): ResponseEntity<Void> {
        return if (agentService.deregisterAgent(id)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
