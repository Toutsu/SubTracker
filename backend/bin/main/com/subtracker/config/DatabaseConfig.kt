package com.subtracker.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.springframework.core.env.Environment
import org.springframework.context.annotation.Primary

@Configuration
class DatabaseConfig(private val env: Environment) {
    
    @Bean
    @Primary
    fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            driverClassName = env.getProperty("spring.datasource.driver-class-name") ?: "org.sqlite.JDBC"
            jdbcUrl = env.getProperty("spring.datasource.url") ?: "jdbc:sqlite:subtracker.db"
            username = env.getProperty("spring.datasource.username") ?: ""
            password = env.getProperty("spring.datasource.password") ?: ""
            maximumPoolSize = env.getProperty("spring.datasource.hikari.maximum-pool-size", Int::class.java, 3)
            isAutoCommit = env.getProperty("spring.datasource.hikari.auto-commit", Boolean::class.java, false)
            transactionIsolation = env.getProperty("spring.datasource.hikari.transaction-isolation") ?: "TRANSACTION_REPEATABLE_READ"
        }
        
        return HikariDataSource(config)
    }
}