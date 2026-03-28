package com.orchestradashboard.server.model

data class AuthRequest(val apiKey: String)

data class RefreshRequest(val refreshToken: String)
