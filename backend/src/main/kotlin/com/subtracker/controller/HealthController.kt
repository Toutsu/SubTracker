package com.subtracker.controller

import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController(
    private val env: Environment
) {
    
    @GetMapping("/health")
    fun health(): Map<String, Any> {
        val databaseDriver = env.getProperty("spring.datasource.driver-class-name") ?: "org.sqlite.JDBC"
        val databaseType = when {
            databaseDriver.contains("postgresql", ignoreCase = true) -> "PostgreSQL"
            databaseDriver.contains("sqlite", ignoreCase = true) -> "SQLite"
            else -> databaseDriver
        }
        
        return mapOf(
            "status" to "UP",
            "timestamp" to System.currentTimeMillis(),
            "version" to "1.0.0",
            "database" to databaseType,
            "databaseDriver" to databaseDriver
        )
    }
}