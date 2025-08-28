import kotlinx.serialization.Serializable

@Serializable
enum class BillingCycle(val value: String) {
    MONTHLY("monthly"),
    YEARLY("yearly"),
    WEEKLY("weekly")
}

@Serializable
data class Subscription(
    val id: String,
    val userId: String,
    val name: String,
    val price: Double,
    val currency: String,
    val billingCycle: BillingCycle,
    val nextPaymentDate: String, // ISO format date string
    val isActive: Boolean = true
)