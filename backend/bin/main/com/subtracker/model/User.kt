package com.subtracker.model

import jakarta.persistence.*
import java.time.LocalDate
import java.math.BigDecimal

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    val id: Long = 0,
    
    @Column(name = "username", nullable = false, unique = true)
    val username: String,
    
    @Column(name = "email", nullable = false, unique = true)
    val email: String,
    
    @Column(name = "password_hash", nullable = false)
    val passwordHash: String,
    
    @Column(name = "telegram_id")
    val telegramId: Long? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDate = LocalDate.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDate = LocalDate.now()
)