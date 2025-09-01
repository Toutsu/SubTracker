package com.subtracker.repository

import com.subtracker.model.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, String> {
    
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId")
    fun findByUserId(@Param("userId") userId: String): List<Subscription>
    
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.isActive = true")
    fun findActiveByUserId(@Param("userId") userId: String): List<Subscription>
}