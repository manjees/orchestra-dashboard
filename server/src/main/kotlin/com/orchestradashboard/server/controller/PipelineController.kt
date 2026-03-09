package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.CreatePipelineRunRequest
import com.orchestradashboard.server.model.PipelineRunResponse
import com.orchestradashboard.server.model.UpdateStatusRequest
import com.orchestradashboard.server.service.PipelineService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
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
@RequestMapping("/api/v1/pipelines")
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
        @RequestBody request: CreatePipelineRunRequest,
    ): ResponseEntity<PipelineRunResponse> = ResponseEntity.status(HttpStatus.CREATED).body(pipelineService.createPipeline(request))

    @PutMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: String,
        @RequestBody request: UpdateStatusRequest,
    ): ResponseEntity<PipelineRunResponse> = ResponseEntity.ok(pipelineService.updateStatus(id, request.status))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<Void> = ResponseEntity.notFound().build()
}
