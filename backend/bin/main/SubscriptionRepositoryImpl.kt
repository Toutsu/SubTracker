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
        SubscriptionTable.select { SubscriptionTable.userId eq UUID.fromString(userId) }
            .map { rowToSubscription(it) }
    }
    
    suspend fun addSubscription(subscription: SubscriptionData): SubscriptionData = DatabaseFactory.dbQuery {
        val insertStatement = SubscriptionTable.insert {
            it[userId] = UUID.fromString(subscription.userId)
            it[name] = subscription.name
            it[price] = subscription.price
            it[currency] = subscription.currency
            it[billingCycle] = subscription.billingCycle
            it[nextPaymentDate] = subscription.nextPaymentDate
            it[isActive] = subscription.isActive
            it[createdAt] = LocalDate.now()
            it[updatedAt] = LocalDate.now()
        }
        
        subscription.copy(id = insertStatement[SubscriptionTable.id].toString())
    }
    
    suspend fun updateSubscription(id: String, subscription: SubscriptionData): Boolean = DatabaseFactory.dbQuery {
        SubscriptionTable.update({ SubscriptionTable.id eq UUID.fromString(id) }) {
            it[name] = subscription.name
            it[price] = subscription.price
            it[currency] = subscription.currency
            it[billingCycle] = subscription.billingCycle
            it[nextPaymentDate] = subscription.nextPaymentDate
            it[isActive] = subscription.isActive
            it[updatedAt] = LocalDate.now()
        } > 0
    }
    
    suspend fun deleteSubscription(id: String): Boolean = DatabaseFactory.dbQuery {
        SubscriptionTable.deleteWhere { SubscriptionTable.id eq UUID.fromString(id) } > 0
    }
    
    private fun rowToSubscription(row: ResultRow): SubscriptionData = SubscriptionData(
        id = row[SubscriptionTable.id].toString(),
        userId = row[SubscriptionTable.userId].toString(),
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