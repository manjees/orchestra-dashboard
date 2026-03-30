package com.orchestradashboard.server.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "metrics")
data class MetricEntity(
    @Id
    @Column(length = 36)
    val id: String = UUID.randomUUID().toString(),
    @Column(name = "agent_id", nullable = false, length = 36)
    val agentId: String = "",
    @Column(nullable = false, length = 100)
    val name: String = "",
    @Column(nullable = false)
    val value: Double = 0.0,
    @Column(nullable = false, length = 50)
    val unit: String = "",
    @Column(nullable = false)
    val timestamp: Long = 0L,
)
