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
            userId = "test-user-123",
            name = "Test Netflix",
            price = BigDecimal("15.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25)
        )
        
        // Act
        val created = repository.addSubscription(testSubscription)
        val retrieved = repository.getSubscriptionsByUserId("test-user-123")
        
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
            userId = "user1",
            name = "Netflix",
            price = BigDecimal("15.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25)
        )
        
        val subscription2 = SubscriptionData(
            userId = "user2",
            name = "Spotify",
            price = BigDecimal("9.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 20)
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
        val originalSubscription = SubscriptionData(
            userId = "test-user",
            name = "Original Name",
            price = BigDecimal("10.00"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25)
        )
        
        // Act
        val created = repository.addSubscription(originalSubscription)
        val updated = created.copy(
            name = "Updated Name",
            price = BigDecimal("20.00")
        )
        val updateResult = repository.updateSubscription(created.id, updated)
        val retrieved = repository.getSubscriptionsByUserId("test-user")
        
        // Assert
        assertTrue(updateResult)
        assertEquals(1, retrieved.size)
        assertEquals("Updated Name", retrieved[0].name)
        assertEquals(BigDecimal("20.00"), retrieved[0].price)
    }
    
    @Test
    fun `test delete subscription`() = runTest {
        // Arrange
        val subscription = SubscriptionData(
            userId = "test-user",
            name = "To Delete",
            price = BigDecimal("5.99"),
            currency = "USD",
            billingCycle = "monthly",
            nextPaymentDate = LocalDate.of(2024, 12, 25)
        )
        
        // Act
        val created = repository.addSubscription(subscription)
        val deleteResult = repository.deleteSubscription(created.id)
        val retrieved = repository.getSubscriptionsByUserId("test-user")
        
        // Assert
        assertTrue(deleteResult)
        assertEquals(0, retrieved.size)
    }
    
    @Test
    fun `test get subscriptions by user returns empty for non-existent user`() = runTest {
        // Act
        val subscriptions = repository.getSubscriptionsByUserId("non-existent-user")
        
        // Assert
        assertEquals(0, subscriptions.size)
    }
}
