package com.subtracker.service

import com.subtracker.model.Subscription
import com.subtracker.repository.SubscriptionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository
) {
    
    fun getAllSubscriptions(): List<Subscription> {
        return subscriptionRepository.findAll()
    }
    
    fun getSubscriptionsByUserId(userId: String): List<Subscription> {
        return subscriptionRepository.findByUserId(userId)
    }
    
    fun addSubscription(
        userId: String,
        name: String,
        price: BigDecimal,
        currency: String,
        billingPeriod: String,
        nextPayment: LocalDate,
        category: String,
        description: String? = null
    ): Subscription {
        val subscription = Subscription(
            userId = userId,
            name = name,
            price = price,
            currency = currency,
            billingPeriod = billingPeriod,
            nextPayment = nextPayment,
            category = category,
            description = description,
            isActive = true,
            createdAt = LocalDate.now(),
            updatedAt = LocalDate.now()
        )
        
        return subscriptionRepository.save(subscription)
    }
    
    fun deleteSubscription(id: String): Boolean {
        return if (subscriptionRepository.existsById(id)) {
            subscriptionRepository.deleteById(id)
            true
        } else {
            false
        }
    }
}