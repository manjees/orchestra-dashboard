package com.orchestradashboard.server.controller

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pipelines")
class PipelineRunRedirectController {
    @RequestMapping("/**", "")
    fun redirect(request: HttpServletRequest): ResponseEntity<Void> {
        val newPath = request.requestURI.replaceFirst("/api/v1/pipelines", "/api/v1/pipeline-runs")
        val query = if (request.queryString != null) "?${request.queryString}" else ""
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
            .header("Location", "$newPath$query")
            .build()
    }
}
