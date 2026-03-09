package com.orchestradashboard.server.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Global CORS and web MVC configuration.
 * Restricts origins in production via the ALLOWED_ORIGINS environment variable.
 */
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        val allowedOrigins =
            System.getenv("ALLOWED_ORIGINS")
                ?.split(",")
                ?.toTypedArray()
                ?: arrayOf("http://localhost:*")

        registry.addMapping("/api/**")
            .allowedOriginPatterns(*allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }
}
