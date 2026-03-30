package com.orchestradashboard.server.repository

import com.orchestradashboard.server.model.MetricsAggregateEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MetricsAggregateJpaRepository : JpaRepository<MetricsAggregateEntity, String>
