package com.subtracker.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory

class JwtTokenFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(JwtTokenFilter::class.java)
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestPath = request.requestURI.substring(request.contextPath.length)
        logger.debug("Processing request to: $requestPath")
        
        // Skip filter for public endpoints
        val publicPaths = listOf("/health", "/register", "/login", "/api/health", "/api/register", "/api/login", "/error")
        if (publicPaths.any { requestPath.startsWith(it) }) {
            logger.debug("Skipping filter for public path: $requestPath")
            filterChain.doFilter(request, response)
            return
        }
        
        // Skip filter for Swagger UI paths
        val swaggerPaths = listOf("/api/swagger-ui.html", "/api/swagger-ui", "/api/v3/api-docs", "/swagger-ui.html", "/swagger-ui", "/v3/api-docs")
        if (swaggerPaths.any { requestPath.startsWith(it) }) {
            logger.debug("Skipping filter for Swagger path: $requestPath")
            filterChain.doFilter(request, response)
            return
        }
        
        val token = resolveToken(request)
        logger.debug("Token resolved: ${if (token != null) "Present" else "Not present"}")
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            val username = jwtTokenProvider.getUsername(token)
            val userId = jwtTokenProvider.getUserId(token)
            val auth = UsernamePasswordAuthenticationToken(username, userId, emptyList())
            SecurityContextHolder.getContext().authentication = auth
            logger.debug("Authentication set for user: $username")
        } else {
            logger.debug("Token is null or invalid")
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}