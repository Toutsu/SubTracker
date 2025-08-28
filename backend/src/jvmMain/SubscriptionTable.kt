package backend

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object SubscriptionTable : Table("subscriptions") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36)
    val name = varchar("name", 255)
    val price = decimal("price", 10, 2)
    val currency = varchar("currency", 3)
    val billingCycle = varchar("billing_cycle", 50)
    val nextPaymentDate = date("next_payment_date")
    val isActive = bool("is_active").default(true)
    val createdAt = date("created_at")
    val updatedAt = date("updated_at")
    
    override val primaryKey = PrimaryKey(id)
}

object UserTable : Table("users") {
    val id = varchar("id", 36)
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val telegramId = long("telegram_id").nullable()
    val createdAt = date("created_at")
    val updatedAt = date("updated_at")
    
    override val primaryKey = PrimaryKey(id)
}