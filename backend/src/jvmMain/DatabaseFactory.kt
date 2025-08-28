package backend

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.MessageDigest
import java.time.LocalDate

object DatabaseFactory {
    fun init() {
        val driverClassName = System.getenv("DATABASE_DRIVER") ?: "org.sqlite.JDBC"
        val jdbcURL = System.getenv("DATABASE_URL") ?: "jdbc:sqlite:subtracker.db"
        val username = System.getenv("DATABASE_USER") ?: ""
        val password = System.getenv("DATABASE_PASSWORD") ?: ""
        
        val config = HikariConfig().apply {
            this.driverClassName = driverClassName
            this.jdbcUrl = jdbcURL
            this.username = username
            this.password = password
            this.maximumPoolSize = 3
            this.isAutoCommit = false
            this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            this.validate()
        }
        
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        
        // Создаем таблицы
        transaction {
            SchemaUtils.create(UserTable, SubscriptionTable)
            
            // Создаем тестового пользователя если его нет
            val existingUser = UserTable.select { UserTable.username eq "user" }.singleOrNull()
            if (existingUser == null) {
                UserTable.insert {
                    it[UserTable.id] = java.util.UUID.randomUUID().toString()
                    it[UserTable.username] = "user"
                    it[UserTable.email] = "user@test.com"
                    it[UserTable.passwordHash] = hashPassword("user")
                    it[UserTable.telegramId] = null
                    it[UserTable.createdAt] = LocalDate.now()
                    it[UserTable.updatedAt] = LocalDate.now()
                }
                println("Test user created: user/user")
            }
        }
        
        println("Database initialized successfully")
    }
    
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
    
    fun close() {
        // HikariCP автоматически закрывает соединения
        println("Database connections closed")
    }
    
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}