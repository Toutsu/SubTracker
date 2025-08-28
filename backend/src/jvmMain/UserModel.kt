package backend

data class UserModel(
    val id: String,
    val username: String,
    val email: String,
    val passwordHash: String
)
