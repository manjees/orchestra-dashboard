package com.orchestradashboard.server.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/health").permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/projects/**").permitAll()
                    .requestMatchers("/api/v1/system/**").permitAll()
                    .requestMatchers("/api/v1/orchestrator/**").permitAll()
                    .requestMatchers("/api/v1/proxy/commands/**").permitAll()
                    .requestMatchers("/api/v1/approvals/**").permitAll()
                    .requestMatchers("/api/v1/checkpoints/**").permitAll()
                    .requestMatchers("/api/v1/pipeline-history/**").permitAll()
                    .requestMatchers("/api/v1/analytics/**").permitAll()
                    .requestMatchers("/api/v1/notifications/**").permitAll()
                    .requestMatchers("/ws/**").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
            .headers { it.frameOptions { fo -> fo.sameOrigin() } }
        return http.build()
    }
}
