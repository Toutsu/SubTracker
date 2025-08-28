import backend.DatabaseFactory
import backend.UserTable
import backend.SubscriptionTable
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DatabaseFactoryTest {
    
    @BeforeEach
    fun setup() {
        // Устанавливаем тестовые переменные окружения
        System.setProperty("DATABASE_URL", "jdbc:h2:mem:dbFactoryTest")
        System.setProperty("DATABASE_USER", "sa")
        System.setProperty("DATABASE_PASSWORD", "")
    }
    
    @AfterEach
    fun cleanup() {
        DatabaseFactory.close()
        // Очищаем системные свойства
        System.clearProperty("DATABASE_URL")
        System.clearProperty("DATABASE_USER")
        System.clearProperty("DATABASE_PASSWORD")
    }
    
    @Test
    fun `test database initialization`() {
        // Act
        DatabaseFactory.init()
        
        // Assert - если инициализация прошла без исключений, тест пройден
        assertTrue(true)
    }
    
    @Test
    fun `test database initialization with default values`() {
        // Arrange - очищаем переменные окружения для использования значений по умолчанию
        System.clearProperty("DATABASE_URL")
        System.clearProperty("DATABASE_USER")
        System.clearProperty("DATABASE_PASSWORD")
        
        // Act
        DatabaseFactory.init()
        
        // Assert - если инициализация прошла без исключений, тест пройден
        assertTrue(true)
    }
    
    @Test
    fun `test database close`() {
        // Arrange
        DatabaseFactory.init()
        
        // Act
        DatabaseFactory.close()
        
        // Assert - если закрытие прошло без исключений, тест пройден
        assertTrue(true)
    }
    
    @Test
    fun `test dbQuery execution`() = runTest {
        // Arrange
        DatabaseFactory.init()
        
        // Act
        val result = DatabaseFactory.dbQuery {
            "test result"
        }
        
        // Assert
        assertTrue(result == "test result")
    }
    
    @Test
    fun `test dbQuery with exception`() = runTest {
        // Arrange
        DatabaseFactory.init()
        
        // Act & Assert
        try {
            DatabaseFactory.dbQuery {
                throw RuntimeException("Test exception")
            }
            assertTrue(false, "Expected exception was not thrown")
        } catch (e: RuntimeException) {
            assertTrue(e.message == "Test exception")
        }
    }
    
    @Test
    fun `test multiple database operations`() = runTest {
        // Arrange
        DatabaseFactory.init()
        
        // Act
        val result1 = DatabaseFactory.dbQuery { 1 }
        val result2 = DatabaseFactory.dbQuery { 2 }
        val result3 = DatabaseFactory.dbQuery { result1 + result2 }
        
        // Assert
        assertTrue(result1 == 1)
        assertTrue(result2 == 2)
        assertTrue(result3 == 3)
    }
}
