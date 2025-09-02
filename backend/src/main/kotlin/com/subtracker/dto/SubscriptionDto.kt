package com.subtracker.dto

import java.math.BigDecimal
import java.time.LocalDate

data class SubscriptionResponse(
    val id: String,
    val userId: String,
    val name: String,
    val price: String,
    val currency: String,
    val billingPeriod: String,
    val nextPayment: String,
    val category: String,
    val description: String? = null,
    val isActive: Boolean = true
)

data class CreateSubscriptionRequest(
    val userId: String? = null,
    val name: String,
    val price: String,
    val currency: String,
    val billingPeriod: String,
    val nextPayment: String,
    val category: String,
    val description: String? = null
)