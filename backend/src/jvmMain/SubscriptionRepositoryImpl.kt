package backend

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

class SubscriptionRepositoryImpl {
    
    suspend fun getAllSubscriptions(): List<SubscriptionData> = DatabaseFactory.dbQuery {
        SubscriptionTable.selectAll().map { rowToSubscription(it) }
    }
    
    suspend fun getSubscriptionsByUserId(userId: String): List<SubscriptionData> = DatabaseFactory.dbQuery {
        SubscriptionTable.select { SubscriptionTable.userId eq userId }
            .map { rowToSubscription(it) }
    }
    
    suspend fun addSubscription(subscription: SubscriptionData): SubscriptionData = DatabaseFactory.dbQuery {
        val subscriptionId = UUID.randomUUID().toString()
        SubscriptionTable.insert {
            it[SubscriptionTable.id] = subscriptionId
            it[SubscriptionTable.userId] = subscription.userId
            it[SubscriptionTable.name] = subscription.name
            it[SubscriptionTable.price] = subscription.price
            it[SubscriptionTable.currency] = subscription.currency
            it[SubscriptionTable.billingCycle] = subscription.billingCycle
            it[SubscriptionTable.nextPaymentDate] = subscription.nextPaymentDate
            it[SubscriptionTable.isActive] = subscription.isActive
            it[SubscriptionTable.createdAt] = LocalDate.now()
            it[SubscriptionTable.updatedAt] = LocalDate.now()
        }
        
        subscription.copy(id = subscriptionId)
    }
    
    suspend fun updateSubscription(id: String, subscription: SubscriptionData): Boolean = DatabaseFactory.dbQuery {
        SubscriptionTable.update({ SubscriptionTable.id eq id }) {
            it[SubscriptionTable.name] = subscription.name
            it[SubscriptionTable.price] = subscription.price
            it[SubscriptionTable.currency] = subscription.currency
            it[SubscriptionTable.billingCycle] = subscription.billingCycle
            it[SubscriptionTable.nextPaymentDate] = subscription.nextPaymentDate
            it[SubscriptionTable.isActive] = subscription.isActive
            it[SubscriptionTable.updatedAt] = LocalDate.now()
        } > 0
    }
    
    suspend fun deleteSubscription(id: String): Boolean = DatabaseFactory.dbQuery {
        SubscriptionTable.deleteWhere { SubscriptionTable.id eq id } > 0
    }
    
    private fun rowToSubscription(row: ResultRow): SubscriptionData = SubscriptionData(
        id = row[SubscriptionTable.id],
        userId = row[SubscriptionTable.userId],
        name = row[SubscriptionTable.name],
        price = row[SubscriptionTable.price],
        currency = row[SubscriptionTable.currency],
        billingCycle = row[SubscriptionTable.billingCycle],
        nextPaymentDate = row[SubscriptionTable.nextPaymentDate],
        isActive = row[SubscriptionTable.isActive],
        createdAt = row[SubscriptionTable.createdAt],
        updatedAt = row[SubscriptionTable.updatedAt]
    )
}

data class SubscriptionData(
    val id: String = "",
    val userId: String,
    val name: String,
    val price: BigDecimal,
    val currency: String,
    val billingCycle: String,
    val nextPaymentDate: LocalDate,
    val isActive: Boolean = true,
    val createdAt: LocalDate = LocalDate.now(),
    val updatedAt: LocalDate = LocalDate.now()
)