package backend

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import java.util.UUID

object SubscriptionTable : UUIDTable("subscriptions") {
    val userId = reference("user_id", UserTable.id)
    val name = varchar("name", 255)
    val price = decimal("price", 10, 2)
    val currency = varchar("currency", 3)
    val billingCycle = varchar("billing_cycle", 50)
    val nextPaymentDate = date("next_payment_date")
    val isActive = bool("is_active").default(true)
    val createdAt = date("created_at")
    val updatedAt = date("updated_at")
}

object UserTable : UUIDTable("users") {
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 255).uniqueIndex()
    val telegramId = long("telegram_id").nullable()
    val createdAt = date("created_at")
    val updatedAt = date("updated_at")
}