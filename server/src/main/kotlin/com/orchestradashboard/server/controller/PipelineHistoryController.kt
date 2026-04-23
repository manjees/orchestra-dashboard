package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.PipelineHistoryResponse
import com.orchestradashboard.server.service.PipelineHistoryService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pipeline-history")
class PipelineHistoryController(
    private val historyService: PipelineHistoryService,
) {
    @GetMapping
    fun getHistory(
        @RequestParam project: String?,
        @RequestParam status: String?,
        @RequestParam q: String?,
        @RequestParam hours: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ResponseEntity<Page<PipelineHistoryResponse>> {
        val clampedSize = size.coerceIn(1, 100)
        val pageable = PageRequest.of(page, clampedSize)
        val nowMs = System.currentTimeMillis()
        val fromMs = hours?.let { nowMs - it.toLong() * 3_600_000L }
        val toMs = hours?.let { nowMs }
        return ResponseEntity.ok(historyService.getHistory(project, status, q, fromMs, toMs, pageable))
    }

    @GetMapping("/{id}")
    fun getHistoryById(
        @PathVariable id: String,
    ): ResponseEntity<PipelineHistoryResponse> = ResponseEntity.ok(historyService.getHistoryById(id))

    @Suppress("UnusedPrivateMember")
    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<Void> = ResponseEntity.notFound().build()
}
