package com.orchestradashboard.server.controller

import com.orchestradashboard.server.service.OrchestratorProxyService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/approvals")
class ApprovalProxyController(
    private val proxyService: OrchestratorProxyService,
) {
    @PostMapping("/{approvalId}/respond")
    fun respondToApproval(
        @PathVariable approvalId: String,
        @RequestBody body: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.respondToApproval(approvalId, body))
}
