package com.orchestradashboard.server.controller

import com.orchestradashboard.server.service.OrchestratorProxyService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/commands")
class CommandProxyController(
    private val proxyService: OrchestratorProxyService,
) {
    @PostMapping("/solve")
    fun postSolve(
        @RequestBody body: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.postSolve(body))

    @PostMapping("/init")
    fun postInit(
        @RequestBody body: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.postInit(body))

    @PostMapping("/plan")
    fun postPlan(
        @RequestBody body: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.postPlan(body))

    @PostMapping("/discuss")
    fun postDiscuss(
        @RequestBody body: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.postDiscuss(body))

    @PostMapping("/design")
    fun postDesign(
        @RequestBody body: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.postDesign(body))

    @PostMapping("/shell")
    fun postShell(
        @RequestBody body: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.postShell(body))
}
