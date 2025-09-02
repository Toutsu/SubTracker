package com.subtracker.controller

import com.subtracker.dto.CreateSubscriptionRequest
import com.subtracker.dto.SubscriptionResponse
import com.subtracker.service.SubscriptionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder

@RestController
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SubscriptionController::class.java)
    }
    
    @GetMapping("/subscriptions")
    fun getAllSubscriptions(): ResponseEntity<List<SubscriptionResponse>> {
        logger.info("Received request to get all subscriptions")
        try {
            val subscriptions = subscriptionService.getAllSubscriptions()
            val response = subscriptions.map { subscription ->
                SubscriptionResponse(
                    id = subscription.id!!, // id is guaranteed to be non-null after saving to DB
                    userId = subscription.userId,
                    name = subscription.name,
                    price = subscription.price.toString(),
                    currency = subscription.currency,
                    billingPeriod = subscription.billingPeriod,
                    nextPayment = subscription.nextPayment.toString(),
                    category = subscription.category,
                    description = subscription.description,
                    isActive = subscription.isActive
                )
            }
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(emptyList())
        }
    }
    
    @GetMapping("/subscriptions/{userId}")
    fun getSubscriptionsByUserId(@PathVariable userId: String): ResponseEntity<List<SubscriptionResponse>> {
        logger.info("Received request to get subscriptions for user ID: $userId")
        try {
            val subscriptions = subscriptionService.getSubscriptionsByUserId(userId)
            val response = subscriptions.map { subscription ->
                SubscriptionResponse(
                    id = subscription.id!!, // id is guaranteed to be non-null after saving to DB
                    userId = subscription.userId,
                    name = subscription.name,
                    price = subscription.price.toString(),
                    currency = subscription.currency,
                    billingPeriod = subscription.billingPeriod,
                    nextPayment = subscription.nextPayment.toString(),
                    category = subscription.category,
                    description = subscription.description,
                    isActive = subscription.isActive
                )
            }
            return ResponseEntity.ok(response)
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(emptyList())
        }
    }
    
    
    @PostMapping("/subscriptions")
    fun createSubscription(@RequestBody request: CreateSubscriptionRequest): ResponseEntity<SubscriptionResponse> {
        val authentication = SecurityContextHolder.getContext().authentication
        val userId = authentication.credentials as String
        val userIdToUse = request.userId ?: userId
        
        try {
            val subscription = subscriptionService.addSubscription(
                userId = userIdToUse,
                name = request.name,
                price = BigDecimal(request.price),
                currency = request.currency,
                billingPeriod = request.billingPeriod,
                nextPayment = LocalDate.parse(request.nextPayment),
                category = request.category,
                description = request.description
            )
            
            val response = SubscriptionResponse(
                id = subscription.id!!, // id is guaranteed to be non-null after saving to DB
                userId = subscription.userId,
                name = subscription.name,
                price = subscription.price.toString(),
                currency = subscription.currency,
                billingPeriod = subscription.billingPeriod,
                nextPayment = subscription.nextPayment.toString(),
                category = subscription.category,
                description = subscription.description,
                isActive = subscription.isActive
            )
            
            return ResponseEntity.status(201).body(response)
        } catch (e: Exception) {
            throw e
        }
    }
    
    @DeleteMapping("/subscriptions/{id}")
    fun deleteSubscription(@PathVariable id: String): ResponseEntity<Void> {
        try {
            val deleted = subscriptionService.deleteSubscription(id)
            return if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }
}