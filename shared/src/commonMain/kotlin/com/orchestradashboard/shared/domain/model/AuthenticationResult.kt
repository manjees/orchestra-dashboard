package com.orchestradashboard.shared.domain.model

data class AuthenticationResult(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer",
)
