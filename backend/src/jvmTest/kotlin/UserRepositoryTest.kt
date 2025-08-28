import backend.UserRepositoryImpl
import backend.User
import backend.DatabaseFactory
import backend.UserTable
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest {
    
    private lateinit var repository: UserRepositoryImpl
    
    @BeforeEach
    fun setup() {
        // Инициализируем тестовую базу данных
        System.setProperty("DATABASE_URL", "jdbc:h2:mem:userTestDb")
        System.setProperty("DATABASE_USER", "sa")
        System.setProperty("DATABASE_PASSWORD", "")
        
        DatabaseFactory.init()
        repository = UserRepositoryImpl()
    }
    
    @AfterEach
    fun cleanup() {
        DatabaseFactory.close()
    }
    
    @Test
    fun `test create user`() = runTest {
        // Arrange
        val username = "testuser"
        val password = "testpassword"
        
        // Act
        val user = repository.createUser(username, password)
        
        // Assert
        assertNotNull(user.id)
        assertEquals(username, user.username)
        assertNotNull(user.passwordHash)
        assertTrue(user.passwordHash.isNotEmpty())
    }
    
    @Test
    fun `test get user by username`() = runTest {
        // Arrange
        val username = "finduser"
        val password = "findpassword"
        
        // Act
        val createdUser = repository.createUser(username, password)
        val foundUser = repository.getUserByUsername(username)
        
        // Assert
        assertNotNull(foundUser)
        assertEquals(createdUser.id, foundUser?.id)
        assertEquals(username, foundUser?.username)
        assertEquals(createdUser.passwordHash, foundUser?.passwordHash)
    }
    
    @Test
    fun `test get user by username returns null for non-existent user`() = runTest {
        // Act
        val user = repository.getUserByUsername("nonexistentuser")
        
        // Assert
        assertNull(user)
    }
    
    @Test
    fun `test create multiple users`() = runTest {
        // Arrange & Act
        val user1 = repository.createUser("user1", "pass1")
        val user2 = repository.createUser("user2", "pass2")
        
        // Assert
        assertNotNull(user1.id)
        assertNotNull(user2.id)
        assertTrue(user1.id != user2.id)
        assertEquals("user1", user1.username)
        assertEquals("user2", user2.username)
        assertTrue(user1.passwordHash != user2.passwordHash)
    }
    
    @Test
    fun `test password hashing consistency`() = runTest {
        // Arrange
        val username = "hashuser"
        val password = "hashpassword"
        
        // Act
        val user1 = repository.createUser(username + "1", password)
        val user2 = repository.createUser(username + "2", password)
        
        // Assert - одинаковые пароли должны давать одинаковые хеши
        assertEquals(user1.passwordHash, user2.passwordHash)
    }
}
