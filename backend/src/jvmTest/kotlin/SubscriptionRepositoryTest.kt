import backend.SubscriptionRepositoryImpl
import backend.SubscriptionData
import backend.DatabaseFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import java.math.BigDecimal
import java.time.LocalDate

class SubscriptionRepositoryTest {
    
    private lateinit var repository: SubscriptionRepositoryImpl
    
    @BeforeEach
    fun setup() {
        // Инициализируем тестовую базу данных
        System.setProperty("DATABASE_URL", "jdbc:h2:mem:testdb")
        System.setProperty("DATABASE_USER", "sa")
        System.setProperty("DATABASE_PASSWORD", "")
        
        DatabaseFactory.init()
        repository = SubscriptionRepositoryImpl()
    }
    
    @AfterEach
    fun cleanup() {
        DatabaseFactory.close()
    }
    
    @Test
    fun `test create and retrieve subscription`() = runTest {
        // Arrange
        val testSubscription = SubscriptionData(
            userId = "6e6c5f5e-5e6f-4c5d-9e8f-1a2b3c4d5e6f",
            name = "Test Netflix",
            price = BigDecimal("15.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25),
            isActive = true
        )
        
        // Act
        val created = repository.addSubscription(testSubscription)
        val retrieved = repository.getSubscriptionsByUserId("6e6c5f5e-5e6f-4c5d-9e8f-1a2b3c4d5e6f")
        
        // Assert
        assertNotNull(created.id)
        assertEquals(1, retrieved.size)
        assertEquals("Test Netflix", retrieved[0].name)
        assertEquals(BigDecimal("15.99"), retrieved[0].price)
        assertEquals("USD", retrieved[0].currency)
    }
    
    @Test
    fun `test get all subscriptions`() = runTest {
        // Arrange
        val subscription1 = SubscriptionData(
            userId = "11111111-1111-1111-1111-111111111111",
            name = "Netflix",
            price = BigDecimal("15.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25),
            isActive = true
        )
        
        val subscription2 = SubscriptionData(
            userId = "22222222-2222-2222-2222-222222222222",
            name = "Spotify",
            price = BigDecimal("9.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 20),
            isActive = true
        )
        
        // Act
        repository.addSubscription(subscription1)
        repository.addSubscription(subscription2)
        val allSubscriptions = repository.getAllSubscriptions()
        
        // Assert
        assertEquals(2, allSubscriptions.size)
    }
    
    @Test
    fun `test update subscription`() = runTest {
        // Arrange
        val userId = "33333333-3333-3333-3333-333333333333"
        val originalSubscription = SubscriptionData(
            userId = userId,
            name = "Original Name",
            price = BigDecimal("10.00"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25),
            isActive = true
        )
        
        // Act
        val created = repository.addSubscription(originalSubscription)
        val updated = created.copy(
            name = "Updated Name",
            price = BigDecimal("20.00")
        )
        val updateResult = repository.updateSubscription(created.id, updated)
        val retrieved = repository.getSubscriptionsByUserId(userId)
        
        // Assert
        assertTrue(updateResult)
        assertEquals(1, retrieved.size)
        assertEquals("Updated Name", retrieved[0].name)
        assertEquals(BigDecimal("20.00"), retrieved[0].price)
    }
    
    @Test
    fun `test delete subscription`() = runTest {
        // Arrange
        val userId = "44444444-4444-4444-4444-444444444444"
        val subscription = SubscriptionData(
            userId = userId,
            name = "To Delete",
            price = BigDecimal("5.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25),
            isActive = true
        )
        
        // Act
        val created = repository.addSubscription(subscription)
        val deleteResult = repository.deleteSubscription(created.id)
        val retrieved = repository.getSubscriptionsByUserId(userId)
        
        // Assert
        assertTrue(deleteResult)
        assertEquals(0, retrieved.size)
    }
    
    @Test
    fun `test get subscriptions by user returns empty for non-existent user`() = runTest {
        // Act
        val subscriptions = repository.getSubscriptionsByUserId("00000000-0000-0000-0000-000000000000")
        
        // Assert
        assertEquals(0, subscriptions.size)
    }
    @Test
    fun `test subscription with inactive status`() = runTest {
        // Arrange
        val userId = "55555555-5555-5555-5555-555555555555"
        val testSubscription = SubscriptionData(
            userId = userId,
            name = "Inactive Service",
            price = BigDecimal("9.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25),
            isActive = false
        )
        
        // Act
        repository.addSubscription(testSubscription)
        val retrieved = repository.getSubscriptionsByUserId(userId)
        
        // Assert
        assertEquals(1, retrieved.size)
        assertEquals(false, retrieved[0].isActive)
    }
    
    @Test
    fun `test createdAt and updatedAt fields`() = runTest {
        // Arrange
        val userId = "66666666-6666-6666-6666-666666666666"
        val testSubscription = SubscriptionData(
            userId = userId,
            name = "Date Test",
            price = BigDecimal("12.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25),
            isActive = true
        )
        
        // Act
        val created = repository.addSubscription(testSubscription)
        val retrieved = repository.getSubscriptionsByUserId(userId)
        
        // Assert
        assertEquals(LocalDate.now(), retrieved[0].createdAt)
        assertEquals(LocalDate.now(), retrieved[0].updatedAt)
    }
}
