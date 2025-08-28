import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SubscriptionTest {
    
    @Test
    fun testSubscriptionCreation() {
        // Arrange & Act
        val subscription = Subscription(
            id = "test-123",
            userId = "user-456",
            name = "Test Subscription",
            price = BigDecimal("19.99"),
            currency = "USD",
            billingCycle = BillingCycle.MONTHLY,
            nextPaymentDate = LocalDate.of(2024, 12, 25),
            isActive = true
        )
        
        // Assert
        assertEquals("test-123", subscription.id)
        assertEquals("user-456", subscription.userId)
        assertEquals("Test Subscription", subscription.name)
        assertEquals(BigDecimal("19.99"), subscription.price)
        assertEquals("USD", subscription.currency)
        assertEquals(BillingCycle.MONTHLY, subscription.billingCycle)
        assertEquals(LocalDate.of(2024, 12, 25), subscription.nextPaymentDate)
        assertEquals(true, subscription.isActive)
    }
    
    @Test
    fun testUserCreation() {
        // Arrange & Act
        val user = User(
            id = "user-123",
            username = "testuser",
            email = "test@example.com",
            telegramId = 123456789L
        )
        
        // Assert
        assertEquals("user-123", user.id)
        assertEquals("testuser", user.username)
        assertEquals("test@example.com", user.email)
        assertEquals(123456789L, user.telegramId)
    }
    
    @Test
    fun testBillingCycleValues() {
        // Assert
        assertEquals("monthly", BillingCycle.MONTHLY.value)
        assertEquals("yearly", BillingCycle.YEARLY.value)
        assertEquals("weekly", BillingCycle.WEEKLY.value)
    }
}
