package com.orchestradashboard.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrchestraDashboardApplication

fun main(args: Array<String>) {
    runApplication<OrchestraDashboardApplication>(*args)
}
