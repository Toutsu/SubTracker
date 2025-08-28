import backend.DatabaseFactory
import backend.SubscriptionRepositoryImpl
import backend.UserRepositoryImpl
import backend.SecurityUtils
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.plugins.contentnegotiation.*
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
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String
)

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val subscriptionRepository = SubscriptionRepositoryImpl()
    val userRepository = UserRepositoryImpl()
    
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
    
    routing {
        // Эндпоинт для входа
        post("/login") {
            val loginRequest = call.receive<LoginRequest>()
            val user = userRepository.getUserByUsername(loginRequest.username)
            
            if (user != null && SecurityUtils.verifyPassword(loginRequest.password, user.passwordHash)) {
                val token = JWT.create()
                    .withAudience(jwtAudience)
                    .withIssuer(jwtIssuer)
                    .withClaim("username", user.username)
                    .sign(Algorithm.HMAC256(jwtSecret))
                call.respond(LoginResponse(token))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
        
        // Эндпоинт для регистрации
        post("/register") {
            val loginRequest = call.receive<LoginRequest>()
            try {
                val user = userRepository.createUser(loginRequest.username, loginRequest.password)
                call.respond(HttpStatusCode.Created, mapOf("message" to "User created"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "User creation failed: ${e.message}"))
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