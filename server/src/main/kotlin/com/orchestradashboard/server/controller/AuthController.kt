package com.orchestradashboard.server.controller

import com.orchestradashboard.server.config.JwtTokenProvider
import com.orchestradashboard.server.model.AuthRequest
import com.orchestradashboard.server.model.AuthResponse
import com.orchestradashboard.server.model.RefreshRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${auth.api-key}") private val configuredApiKey: String,
) {
    @PostMapping("/login")
    fun login(
        @RequestBody request: AuthRequest,
    ): ResponseEntity<AuthResponse> {
        if (request.apiKey != configuredApiKey) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val subject = "dashboard-client"
        return ResponseEntity.ok(
            AuthResponse(
                accessToken = jwtTokenProvider.generateAccessToken(subject),
                refreshToken = jwtTokenProvider.generateRefreshToken(subject),
                expiresIn = jwtTokenProvider.getAccessExpirationMs() / 1000,
            ),
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody request: RefreshRequest,
    ): ResponseEntity<AuthResponse> {
        if (!jwtTokenProvider.validateToken(request.refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val subject = jwtTokenProvider.getSubject(request.refreshToken)
        return ResponseEntity.ok(
            AuthResponse(
                accessToken = jwtTokenProvider.generateAccessToken(subject),
                refreshToken = jwtTokenProvider.generateRefreshToken(subject),
                expiresIn = jwtTokenProvider.getAccessExpirationMs() / 1000,
            ),
        )
    }
}
