package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.AgentCommandResponse
import com.orchestradashboard.server.model.CreateCommandRequest
import com.orchestradashboard.server.service.CommandService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/commands")
class CommandController(
    private val commandService: CommandService,
) {
    @PostMapping
    fun createCommand(
        @Valid @RequestBody request: CreateCommandRequest,
        authentication: Authentication,
    ): ResponseEntity<AgentCommandResponse> {
        val requestedBy = authentication.name
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(commandService.createCommand(request, requestedBy))
    }

    @GetMapping
    fun getCommands(
        @RequestParam agentId: String,
        @RequestParam(required = false) limit: Int?,
    ): ResponseEntity<List<AgentCommandResponse>> = ResponseEntity.ok(commandService.getCommandsByAgentId(agentId, limit))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<Void> = ResponseEntity.notFound().build()

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to (ex.message ?: "Bad request")))

    @ExceptionHandler(IllegalStateException::class)
    fun handleConflict(ex: IllegalStateException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to (ex.message ?: "Conflict")))
}
