import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscriptionTest {
    
    @Test
    fun testSubscriptionSerialization() {
        // Arrange
        val subscription = Subscription(
            id = "test-id",
            userId = "user123",
            name = "Test Service",
            price = "15.99",
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = "2024-12-25",
            isActive = true
        )
        
        // Act
        val json = Json.encodeToString(subscription)
        val deserialized = Json.decodeFromString<Subscription>(json)
        
        // Assert
        assertEquals(subscription.id, deserialized.id)
        assertEquals(subscription.name, deserialized.name)
        assertEquals(subscription.price, deserialized.price)
        assertEquals(subscription.currency, deserialized.currency)
        assertEquals(subscription.isActive, deserialized.isActive)
    }
    
    @Test
    fun testCreateSubscriptionRequestSerialization() {
        // Arrange
        val request = CreateSubscriptionRequest(
            userId = "user123",
            name = "Netflix",
            price = "15.99",
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = "2024-12-25"
        )
        
        // Act
        val json = Json.encodeToString(request)
        val deserialized = Json.decodeFromString<CreateSubscriptionRequest>(json)
        
        // Assert
        assertEquals(request.userId, deserialized.userId)
        assertEquals(request.name, deserialized.name)
        assertEquals(request.price, deserialized.price)
        assertEquals(request.currency, deserialized.currency)
        assertEquals(request.billingCycle, deserialized.billingCycle)
        assertEquals(request.nextPaymentDate, deserialized.nextPaymentDate)
    }
    
    @Test
    fun testTranslateBillingCycle() {
        // Arrange & Act & Assert
        assertEquals("Ежемесячно", translateBillingCycle("monthly"))
        assertEquals("Ежегодно", translateBillingCycle("yearly"))
        assertEquals("Еженедельно", translateBillingCycle("weekly"))
        assertEquals("custom", translateBillingCycle("custom"))
    }
    
    private fun translateBillingCycle(cycle: String): String {
        return when (cycle) {
            "monthly" -> "Ежемесячно"
            "yearly" -> "Ежегодно"
            "weekly" -> "Еженедельно"
            else -> cycle
        }
    }
}
