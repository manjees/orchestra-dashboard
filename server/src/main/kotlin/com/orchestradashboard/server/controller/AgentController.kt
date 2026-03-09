package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.AgentRegistrationRequest
import com.orchestradashboard.server.model.AgentResponse
import com.orchestradashboard.server.model.HeartbeatRequest
import com.orchestradashboard.server.service.AgentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/agents")
class AgentController(
    private val agentService: AgentService,
) {
    @GetMapping
    fun getAgents(
        @RequestParam status: String?,
    ): ResponseEntity<List<AgentResponse>> {
        val agents =
            if (status != null) {
                agentService.getAgentsByStatus(status)
            } else {
                agentService.getAllAgents()
            }
        return ResponseEntity.ok(agents)
    }

    @GetMapping("/{id}")
    fun getAgent(
        @PathVariable id: String,
    ): ResponseEntity<AgentResponse> = ResponseEntity.ok(agentService.getAgent(id))

    @PostMapping
    fun registerAgent(
        @RequestBody request: AgentRegistrationRequest,
    ): ResponseEntity<AgentResponse> = ResponseEntity.status(HttpStatus.CREATED).body(agentService.registerAgent(request))

    @PutMapping("/{id}/heartbeat")
    fun updateHeartbeat(
        @PathVariable id: String,
        @RequestBody request: HeartbeatRequest,
    ): ResponseEntity<AgentResponse> = ResponseEntity.ok(agentService.updateHeartbeat(id, request.status))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<Void> = ResponseEntity.notFound().build()
}
