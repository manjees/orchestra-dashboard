package com.orchestradashboard.server.model

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank

/**
 * JPA entity representing a monitored AI agent stored in the database.
 */
@Entity
@Table(name = "agents")
data class AgentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @field:NotBlank
    @Column(name = "agent_id", unique = true, nullable = false)
    val agentId: String = "",
    @field:NotBlank
    @Column(nullable = false)
    val name: String = "",
    @Column(nullable = false)
    val type: String = "WORKER",
    @Column(nullable = false)
    val status: String = "OFFLINE",
    @Column(name = "last_heartbeat", nullable = false)
    val lastHeartbeat: Long = 0L,
    @ElementCollection(fetch = FetchType.EAGER)
    val metadata: Map<String, String> = emptyMap(),
)
