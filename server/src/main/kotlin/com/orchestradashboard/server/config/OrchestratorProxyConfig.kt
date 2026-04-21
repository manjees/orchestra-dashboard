package com.orchestradashboard.server.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class OrchestratorProxyConfig {
    companion object {
        private const val MAX_IN_MEMORY_SIZE = 2 * 1024 * 1024
    }

    @Value("\${dashboard.orchestrator.api-url}")
    private lateinit var orchestratorUrl: String

    @Value("\${dashboard.orchestrator.api-key:}")
    private lateinit var orchestratorApiKey: String

    @Bean
    fun orchestratorWebClient(builder: WebClient.Builder): WebClient =
        builder
            .baseUrl(orchestratorUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("X-API-Key", orchestratorApiKey)
            .codecs { it.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE) }
            .build()
}
