package com.subtracker.dto

import java.math.BigDecimal
import java.time.LocalDate

data class SubscriptionResponse(
    val id: String,
    val userId: String,
    val name: String,
    val price: String,
    val currency: String,
    val billingCycle: String,
    val nextPaymentDate: String,
    val isActive: Boolean = true
)

data class CreateSubscriptionRequest(
    val userId: String,
    val name: String,
    val price: String,
    val currency: String,
    val billingCycle: String,
    val nextPaymentDate: String
)