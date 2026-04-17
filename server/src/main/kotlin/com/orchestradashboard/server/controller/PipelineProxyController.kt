package com.orchestradashboard.server.controller

import com.orchestradashboard.server.service.OrchestratorProxyService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/orchestrator/pipelines")
class PipelineProxyController(
    private val proxyService: OrchestratorProxyService,
) {
    @GetMapping
    fun getPipelines(): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.getPipelines())

    @GetMapping("/{id}")
    fun getPipeline(
        @PathVariable id: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.getPipeline(id))

    @GetMapping("/history")
    fun getPipelineHistory(): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.getPipelineHistory())

    @GetMapping("/{parentId}/parallel")
    fun getParallelPipelines(
        @PathVariable parentId: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.getParallelPipelines(parentId))
}
