package com.orchestradashboard.server.model

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthResponse(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("refresh_token") val refreshToken: String,
    @JsonProperty("expires_in") val expiresIn: Long,
    @JsonProperty("token_type") val tokenType: String = "Bearer",
)
