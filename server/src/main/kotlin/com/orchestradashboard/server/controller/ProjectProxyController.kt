package com.orchestradashboard.server.controller

import com.orchestradashboard.server.service.OrchestratorProxyService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/projects")
class ProjectProxyController(
    private val proxyService: OrchestratorProxyService,
) {
    @GetMapping
    fun getProjects(): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.getProjects())

    @GetMapping("/{name}")
    fun getProject(
        @PathVariable name: String,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.getProject(name))

    @GetMapping("/{name}/issues")
    fun getProjectIssues(
        @PathVariable name: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") pageSize: Int,
    ): ResponseEntity<String> =
        ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(proxyService.getProjectIssues(name, page, pageSize))
}
