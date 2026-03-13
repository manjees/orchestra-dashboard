package com.orchestradashboard.server.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-expiration-ms:900000}") private val accessExpirationMs: Long,
    @Value("\${jwt.refresh-expiration-ms:604800000}") private val refreshExpirationMs: Long,
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateAccessToken(subject: String): String = generateToken(subject, accessExpirationMs)

    fun generateRefreshToken(subject: String): String = generateToken(subject, refreshExpirationMs)

    fun validateToken(token: String): Boolean =
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            true
        } catch (
            @Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception,
        ) {
            false
        }

    fun getSubject(token: String): String =
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject

    fun getAccessExpirationMs(): Long = accessExpirationMs

    private fun generateToken(
        subject: String,
        expirationMs: Long,
    ): String {
        val now = Date()
        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(Date(now.time + expirationMs))
            .signWith(key)
            .compact()
    }
}
