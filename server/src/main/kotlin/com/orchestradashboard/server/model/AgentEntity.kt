package com.orchestradashboard.server.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "agents")
data class AgentEntity(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(nullable = false)
    val name: String = "",
    @Column(nullable = false)
    val type: String = "WORKER",
    @Column(nullable = false)
    val status: String = "OFFLINE",
    @Column(name = "last_heartbeat", nullable = false)
    val lastHeartbeat: Long = 0L,
    @Column(columnDefinition = "TEXT")
    val metadata: String = "{}",
)
