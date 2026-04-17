package com.orchestradashboard.server.controller

import com.orchestradashboard.server.service.OrchestratorProxyService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/checkpoints")
class CheckpointProxyController(
    private val proxyService: OrchestratorProxyService,
) {
    @GetMapping
    fun getCheckpoints(): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.getCheckpoints())

    @PostMapping("/{checkpointId}/retry")
    fun retryCheckpoint(
        @PathVariable checkpointId: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.retryCheckpoint(checkpointId))
}
