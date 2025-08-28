import backend.DatabaseFactory
import backend.SubscriptionRepositoryImpl
import backend.UserRepository
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

@Serializable
data class SubscriptionResponse(
    val id: String,
    val userId: String,
    val name: String,
    val price: String,
    val currency: String,
    val billingCycle: String,
    val nextPaymentDate: String,
    val isActive: Boolean = true
)

@Serializable
data class CreateSubscriptionRequest(
    val userId: String,
    val name: String,
    val price: String,
    val currency: String,
    val billingCycle: String,
    val nextPaymentDate: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null
)

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val subscriptionRepository = SubscriptionRepositoryImpl()
    val userRepository = UserRepository()
    
    // Конфигурация JWT
    val jwtSecret = "your_secret_key" // В продакшене использовать переменные окружения
    val jwtIssuer = "SubTracker"
    val jwtAudience = "users"
    val jwtRealm = "ktor.io"
    
    install(Authentication) {
        jwt("jwt") {
            realm = jwtRealm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
    
    install(ContentNegotiation) {
        json()
    }
    
    install(CORS) {
        allowHost("localhost:3000")
        allowHost("127.0.0.1:3000")
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowCredentials = true
    }
    
    routing {
        // Health check endpoint для Docker
        get("/health") {
            call.respond(mapOf(
                "status" to "UP",
                "timestamp" to System.currentTimeMillis(),
                "version" to "1.0.0",
                "database" to "SQLite"
            ))
        }
        
        // Эндпоинт для регистрации
        post("/register") {
            try {
                val registerRequest = call.receive<RegisterRequest>()
                val user = userRepository.createUser(
                    registerRequest.username, 
                    registerRequest.email, 
                    registerRequest.password
                )
                
                if (user != null) {
                    call.respond(AuthResponse(
                        success = true,
                        message = "Пользователь успешно зарегистрирован"
                    ))
                } else {
                    call.respond(HttpStatusCode.BadRequest, AuthResponse(
                        success = false,
                        message = "Пользователь с таким именем или email уже существует"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, AuthResponse(
                    success = false,
                    message = "Ошибка регистрации: ${e.message}"
                ))
            }
        }
        
        // Эндпоинт для входа
        post("/login") {
            try {
                val loginRequest = call.receive<LoginRequest>()
                val user = userRepository.authenticateUser(loginRequest.username, loginRequest.password)
                
                if (user != null) {
                    val token = JWT.create()
                        .withAudience(jwtAudience)
                        .withIssuer(jwtIssuer)
                        .withClaim("username", loginRequest.username)
                        .withClaim("userId", user.id)
                        .sign(Algorithm.HMAC256(jwtSecret))
                        
                    call.respond(AuthResponse(
                        success = true,
                        message = "Вход выполнен успешно",
                        token = token
                    ))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, AuthResponse(
                        success = false,
                        message = "Неверное имя пользователя или пароль"
                    ))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, AuthResponse(
                    success = false,
                    message = "Ошибка входа: ${e.message}"
                ))
            }
        }
        
        // Защищенные эндпоинты
        authenticate("jwt") {
            get("/subscriptions") {
                try {
                    val subscriptions = subscriptionRepository.getAllSubscriptions()
                    val response = subscriptions.map { subscription ->
                        SubscriptionResponse(
                            id = subscription.id,
                            userId = subscription.userId,
                            name = subscription.name,
                            price = subscription.price.toString(),
                            currency = subscription.currency,
                            billingCycle = subscription.billingCycle,
                            nextPaymentDate = subscription.nextPaymentDate.toString(),
                            isActive = subscription.isActive
                        )
                    }
                    call.respond(response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            get("/subscriptions/{userId}") {
                try {
                    val userId = call.parameters["userId"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest, mapOf("error" to "userId is required")
                    )
                    
                    val subscriptions = subscriptionRepository.getSubscriptionsByUserId(userId)
                    val response = subscriptions.map { subscription ->
                        SubscriptionResponse(
                            id = subscription.id,
                            userId = subscription.userId,
                            name = subscription.name,
                            price = subscription.price.toString(),
                            currency = subscription.currency,
                            billingCycle = subscription.billingCycle,
                            nextPaymentDate = subscription.nextPaymentDate.toString(),
                            isActive = subscription.isActive
                        )
                    }
                    call.respond(response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
            
            post("/subscriptions") {
                try {
                    val request = call.receive<CreateSubscriptionRequest>()
                    val subscriptionData = backend.SubscriptionData(
                        userId = request.userId,
                        name = request.name,
                        price = BigDecimal(request.price),
                        currency = request.currency,
                        billingCycle = request.billingCycle,
                        nextPaymentDate = LocalDate.parse(request.nextPaymentDate)
                    )
                    
                    val createdSubscription = subscriptionRepository.addSubscription(subscriptionData)
                    val response = SubscriptionResponse(
                        id = createdSubscription.id,
                        userId = createdSubscription.userId,
                        name = createdSubscription.name,
                        price = createdSubscription.price.toString(),
                        currency = createdSubscription.currency,
                        billingCycle = createdSubscription.billingCycle,
                        nextPaymentDate = createdSubscription.nextPaymentDate.toString(),
                        isActive = createdSubscription.isActive
                    )
                    
                    call.respond(HttpStatusCode.Created, response)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
                }
            }
            
            delete("/subscriptions/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest, mapOf("error" to "id is required")
                    )
                    
                    val deleted = subscriptionRepository.deleteSubscription(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Subscription not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
                }
            }
        }
    }
}