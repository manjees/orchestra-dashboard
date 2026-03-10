package com.orchestradashboard.server.websocket

data class WebSocketMessage<T>(
    val type: String,
    val data: T,
)
