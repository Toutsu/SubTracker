package com.subtracker.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${spring.security.jwt.secret:hW3l9e0bZk6Xg5cFqT1V4sYz8pD2wJ7mUOQyA9vN3I=}") private val jwtSecret: String,
    @Value("\${spring.security.jwt.issuer}") private val jwtIssuer: String,
    @Value("\${spring.security.jwt.audience}") private val jwtAudience: String
) {
    
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    
    fun createToken(username: String, userId: Long): String {
        val now = Date()
        val validity = Date(now.time + 3600000) // 1 hour
        
        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId.toString())
            .setIssuer(jwtIssuer)
            .setAudience(jwtAudience)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }
    
    fun validateToken(token: String): Boolean {
        try {
            getClaims(token)
            return true
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .requireIssuer(jwtIssuer)
            .requireAudience(jwtAudience)
            .build()
            .parseClaimsJws(token)
            .body
    }
    
    fun getUsername(token: String): String {
        return getClaims(token).subject
    }
    
    fun getUserId(token: String): String {
        return getClaims(token).get("userId", String::class.java)
    }
}