package com.subtracker.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "subscriptions")
data class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, unique = true)
    val id: String = "",
    
    @Column(name = "user_id", nullable = false)
    val userId: String,
    
    @Column(name = "name", nullable = false)
    val name: String,
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,
    
    @Column(name = "currency", nullable = false, length = 3)
    val currency: String,
    
    @Column(name = "billing_cycle", nullable = false)
    val billingCycle: String,
    
    @Column(name = "next_payment_date", nullable = false)
    val nextPaymentDate: LocalDate,
    
    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDate = LocalDate.now(),
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDate = LocalDate.now()
)