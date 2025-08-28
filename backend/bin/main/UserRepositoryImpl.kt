package backend

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepositoryImpl {
    suspend fun getUserByUsername(username: String): User? {
        return newSuspendedTransaction {
            UserTable.select { UserTable.username eq username }
                .map {
                    User(
                        id = it[UserTable.id].toString(),
                        username = it[UserTable.username],
                        passwordHash = it[UserTable.passwordHash]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun createUser(username: String, password: String): User {
        return newSuspendedTransaction {
            val hash = SecurityUtils.hashPassword(password)
            val id = UserTable.insert {
                it[UserTable.username] = username
                it[passwordHash] = hash
            } get UserTable.id

            User(
                id = id.toString(),
                username = username,
                passwordHash = hash
            )
        }
    }
}

data class User(
    val id: String,
    val username: String,
    val passwordHash: String
)