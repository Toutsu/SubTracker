import backend.SecurityUtils
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class SecurityUtilsTest {
    
    @Test
    fun `test password hashing`() {
        // Arrange
        val password = "testpassword"
        
        // Act
        val hash = SecurityUtils.hashPassword(password)
        
        // Assert
        assertTrue(hash.isNotEmpty())
        assertEquals(64, hash.length) // SHA-256 hex string length
        assertTrue(hash.matches(Regex("^[a-f0-9]+$"))) // Only hex characters
    }
    
    @Test
    fun `test same password produces same hash`() {
        // Arrange
        val password = "samepassword"
        
        // Act
        val hash1 = SecurityUtils.hashPassword(password)
        val hash2 = SecurityUtils.hashPassword(password)
        
        // Assert
        assertEquals(hash1, hash2)
    }
    
    @Test
    fun `test different passwords produce different hashes`() {
        // Arrange
        val password1 = "password1"
        val password2 = "password2"
        
        // Act
        val hash1 = SecurityUtils.hashPassword(password1)
        val hash2 = SecurityUtils.hashPassword(password2)
        
        // Assert
        assertNotEquals(hash1, hash2)
    }
    
    @Test
    fun `test verify correct password`() {
        // Arrange
        val password = "correctpassword"
        val hash = SecurityUtils.hashPassword(password)
        
        // Act
        val isValid = SecurityUtils.verifyPassword(password, hash)
        
        // Assert
        assertTrue(isValid)
    }
    
    @Test
    fun `test verify incorrect password`() {
        // Arrange
        val correctPassword = "correctpassword"
        val incorrectPassword = "wrongpassword"
        val hash = SecurityUtils.hashPassword(correctPassword)
        
        // Act
        val isValid = SecurityUtils.verifyPassword(incorrectPassword, hash)
        
        // Assert
        assertFalse(isValid)
    }
    
    @Test
    fun `test empty password hashing`() {
        // Arrange
        val password = ""
        
        // Act
        val hash = SecurityUtils.hashPassword(password)
        
        // Assert
        assertTrue(hash.isNotEmpty())
        assertEquals(64, hash.length)
    }
    
    @Test
    fun `test special characters in password`() {
        // Arrange
        val password = "p@ssw0rd!@#$%^&*()"
        
        // Act
        val hash = SecurityUtils.hashPassword(password)
        val isValid = SecurityUtils.verifyPassword(password, hash)
        
        // Assert
        assertTrue(hash.isNotEmpty())
        assertTrue(isValid)
    }
    
    @Test
    fun `test unicode characters in password`() {
        // Arrange
        val password = "пароль123ñáéíóú"
        
        // Act
        val hash = SecurityUtils.hashPassword(password)
        val isValid = SecurityUtils.verifyPassword(password, hash)
        
        // Assert
        assertTrue(hash.isNotEmpty())
        assertTrue(isValid)
    }
    
    @Test
    fun `test long password`() {
        // Arrange
        val password = "a".repeat(1000)
        
        // Act
        val hash = SecurityUtils.hashPassword(password)
        val isValid = SecurityUtils.verifyPassword(password, hash)
        
        // Assert
        assertTrue(hash.isNotEmpty())
        assertEquals(64, hash.length) // Hash length should remain constant
        assertTrue(isValid)
    }
}
