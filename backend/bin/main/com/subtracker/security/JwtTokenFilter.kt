package com.subtracker.security

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class JwtTokenFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {
    
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // Skip filter for public endpoints
        val publicPaths = listOf("/health", "/register", "/login")
        val requestPath = request.requestURI.substring(request.contextPath.length)
        
        if (publicPaths.any { requestPath.startsWith(it) }) {
            filterChain.doFilter(request, response)
            return
        }
        
        val token = resolveToken(request)
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            val username = jwtTokenProvider.getUsername(token)
            val userId = jwtTokenProvider.getUserId(token)
            val auth = UsernamePasswordAuthenticationToken(username, userId, emptyList())
            SecurityContextHolder.getContext().authentication = auth
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