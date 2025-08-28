package backend

import org.jetbrains.exposed.sql.*
import java.security.MessageDigest
import java.time.LocalDate
import java.util.*

class UserRepository {
    
    suspend fun createUser(username: String, email: String, password: String): UserModel? {
        return DatabaseFactory.dbQuery {
            val existingUser = UserTable.select { 
                (UserTable.username eq username) or (UserTable.email eq email) 
            }.singleOrNull()
            
            if (existingUser != null) {
                null // Пользователь уже существует
            } else {
                val userId = UUID.randomUUID().toString()
                UserTable.insert {
                    it[UserTable.id] = userId
                    it[UserTable.username] = username
                    it[UserTable.email] = email
                    it[passwordHash] = hashPassword(password)
                    it[telegramId] = null
                    it[createdAt] = LocalDate.now()
                    it[updatedAt] = LocalDate.now()
                }
                
                UserModel(
                    id = userId,
                    username = username,
                    email = email,
                    passwordHash = hashPassword(password)
                )
            }
        }
    }
    
    suspend fun authenticateUser(username: String, password: String): UserModel? {
        return DatabaseFactory.dbQuery {
            val userRow = UserTable.select { 
                UserTable.username eq username 
            }.singleOrNull()
            
            if (userRow != null && userRow[UserTable.passwordHash] == hashPassword(password)) {
                UserModel(
                    id = userRow[UserTable.id],
                    username = userRow[UserTable.username],
                    email = userRow[UserTable.email],
                    passwordHash = userRow[UserTable.passwordHash]
                )
            } else {
                null
            }
        }
    }
    
    suspend fun getUserById(id: String): UserModel? {
        return DatabaseFactory.dbQuery {
            val userRow = UserTable.select { 
                UserTable.id eq id 
            }.singleOrNull()
            
            userRow?.let {
                UserModel(
                    id = it[UserTable.id],
                    username = it[UserTable.username],
                    email = it[UserTable.email],
                    passwordHash = it[UserTable.passwordHash]
                )
            }
        }
    }
    
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
