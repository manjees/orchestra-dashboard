package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.AgentEventResponse
import com.orchestradashboard.server.model.CreateEventRequest
import com.orchestradashboard.server.service.EventService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventService: EventService,
) {
    @GetMapping
    fun getEvents(
        @RequestParam agentId: String?,
        @RequestParam limit: Int?,
    ): ResponseEntity<List<AgentEventResponse>> {
        val events =
            if (agentId != null) {
                eventService.getEventsByAgentId(agentId, limit)
            } else {
                eventService.getRecentEvents(limit)
            }
        return ResponseEntity.ok(events)
    }

    @PostMapping
    fun createEvent(
        @RequestBody request: CreateEventRequest,
    ): ResponseEntity<AgentEventResponse> = ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request))

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(ex: NoSuchElementException): ResponseEntity<Void> = ResponseEntity.notFound().build()
}
