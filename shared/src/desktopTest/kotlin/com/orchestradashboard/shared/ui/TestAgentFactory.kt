package com.orchestradashboard.shared.ui

import com.orchestradashboard.shared.domain.model.Agent
import kotlinx.datetime.Clock

object TestAgentFactory {
    fun create(
        id: String = "agent-1",
        name: String = "test-agent",
        type: Agent.AgentType = Agent.AgentType.WORKER,
        status: Agent.AgentStatus = Agent.AgentStatus.RUNNING,
        lastHeartbeat: Long = Clock.System.now().toEpochMilliseconds(),
    ): Agent =
        Agent(
            id = id,
            name = name,
            type = type,
            status = status,
            lastHeartbeat = lastHeartbeat,
        )

    fun createList(): List<Agent> =
        listOf(
            create(id = "1", name = "orchestrator-1", type = Agent.AgentType.ORCHESTRATOR, status = Agent.AgentStatus.RUNNING),
            create(id = "2", name = "worker-1", type = Agent.AgentType.WORKER, status = Agent.AgentStatus.IDLE),
            create(id = "3", name = "reviewer-1", type = Agent.AgentType.REVIEWER, status = Agent.AgentStatus.ERROR),
            create(id = "4", name = "planner-1", type = Agent.AgentType.PLANNER, status = Agent.AgentStatus.OFFLINE),
        )
}
