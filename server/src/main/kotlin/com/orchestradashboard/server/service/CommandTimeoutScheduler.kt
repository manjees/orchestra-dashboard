package com.orchestradashboard.server.service

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class CommandTimeoutScheduler(
    private val commandService: CommandService,
) {
    @Scheduled(fixedRate = 10_000)
    fun checkTimeouts() {
        commandService.timeoutStaleCommands()
    }
}
