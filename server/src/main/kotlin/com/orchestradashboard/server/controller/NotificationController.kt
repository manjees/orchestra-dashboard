package com.orchestradashboard.server.controller

import com.orchestradashboard.server.model.notification.DeviceTokenRequest
import com.orchestradashboard.server.model.notification.DeviceTokenResponse
import com.orchestradashboard.server.service.notification.NotificationService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val service: NotificationService,
) {
    @PostMapping("/devices")
    fun register(
        @Valid @RequestBody body: DeviceTokenRequest,
    ): ResponseEntity<DeviceTokenResponse> {
        val response = service.registerToken(body)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/devices/{token}")
    fun unregister(
        @PathVariable token: String,
    ): ResponseEntity<Void> {
        val removed = service.unregisterToken(token)
        return if (removed) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
