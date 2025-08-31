package com.subtracker.controller

import com.subtracker.dto.CreateSubscriptionRequest
import com.subtracker.dto.SubscriptionResponse
import com.subtracker.service.SubscriptionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate

@RestController
@RequestMapping("/api")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    
    @GetMapping("/subscriptions")
    fun getAllSubscriptions(): ResponseEntity<List<SubscriptionResponse>> {
        try {
            val subscriptions = subscriptionService.getAllSubscriptions()
            val response = subscriptions.map { subscription ->
                SubscriptionResponse(
                    id = subscription.id,
                    userId = subscription.userId,
                    name = subscription.name,
                    price = subscription.price.toString(),
                    currency = subscription.currency,
                    billingCycle = subscription.billingCycle,
                    nextPaymentDate = subscription.nextPaymentDate.toString(),
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
        try {
            val subscriptions = subscriptionService.getSubscriptionsByUserId(userId)
            val response = subscriptions.map { subscription ->
                SubscriptionResponse(
                    id = subscription.id,
                    userId = subscription.userId,
                    name = subscription.name,
                    price = subscription.price.toString(),
                    currency = subscription.currency,
                    billingCycle = subscription.billingCycle,
                    nextPaymentDate = subscription.nextPaymentDate.toString(),
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
        try {
            val subscription = subscriptionService.addSubscription(
                userId = request.userId,
                name = request.name,
                price = BigDecimal(request.price),
                currency = request.currency,
                billingCycle = request.billingCycle,
                nextPaymentDate = LocalDate.parse(request.nextPaymentDate)
            )
            
            val response = SubscriptionResponse(
                id = subscription.id,
                userId = subscription.userId,
                name = subscription.name,
                price = subscription.price.toString(),
                currency = subscription.currency,
                billingCycle = subscription.billingCycle,
                nextPaymentDate = subscription.nextPaymentDate.toString(),
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