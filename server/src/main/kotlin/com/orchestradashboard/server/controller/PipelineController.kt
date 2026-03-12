package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.CreatePipelineRunRequest
import com.orchestradashboard.server.model.PatchPipelineRunRequest
import com.orchestradashboard.server.model.PipelineRunResponse
import com.orchestradashboard.server.model.UpdateStatusRequest
import com.orchestradashboard.server.service.PipelineService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pipeline-runs")
class PipelineController(
    private val pipelineService: PipelineService,
) {
    @GetMapping
    fun getPipelines(
        @RequestParam agentId: String?,
        @RequestParam status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<Page<PipelineRunResponse>> {
        val clampedSize = size.coerceIn(1, 100)
        val pageable = PageRequest.of(page, clampedSize)
        return ResponseEntity.ok(pipelineService.getPipelines(agentId, status, pageable))
    }

    @GetMapping("/{id}")
    fun getPipeline(
        @PathVariable id: String,
    ): ResponseEntity<PipelineRunResponse> = ResponseEntity.ok(pipelineService.getPipeline(id))

    @PostMapping
    fun createPipeline(
        @Valid @RequestBody request: CreatePipelineRunRequest,
    ): ResponseEntity<PipelineRunResponse> = ResponseEntity.status(HttpStatus.CREATED).body(pipelineService.createPipeline(request))

    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateStatusRequest,
    ): ResponseEntity<PipelineRunResponse> = ResponseEntity.ok(pipelineService.updateStatus(id, request.status))

    @PatchMapping("/{id}")
    fun updatePipeline(
        @PathVariable id: String,
        @RequestBody request: PatchPipelineRunRequest,
    ): ResponseEntity<PipelineRunResponse> = ResponseEntity.ok(pipelineService.updatePipeline(id, request))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<Void> = ResponseEntity.notFound().build()

    @ExceptionHandler(ObjectOptimisticLockingFailureException::class)
    fun handleConflict(ex: ObjectOptimisticLockingFailureException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT)
            .body(mapOf("error" to "Concurrent modification detected. Please retry."))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to (it.defaultMessage ?: "invalid") }
        return ResponseEntity.badRequest().body(errors)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<Map<String, String>> =
        ResponseEntity.badRequest().body(mapOf("error" to (ex.message ?: "Bad request")))
}
