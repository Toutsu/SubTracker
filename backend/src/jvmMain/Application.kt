import backend.DatabaseFactory
import backend.SubscriptionRepositoryImpl
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate

@Serializable
data class SubscriptionResponse(
    val id: String,
    val userId: String,
    val name: String,
    val price: String, // Изменено для корректной сериализации BigDecimal
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

fun main() {
    // Инициализируем базу данных
    DatabaseFactory.init()
    
    embeddedServer(Netty, port = 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val subscriptionRepository = SubscriptionRepositoryImpl()
    
    // Настройка content negotiation
    install(ContentNegotiation) {
        json()
    }
    
    // Настройка маршрутизации
    routing {
        // Получить все подписки
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
        
        // Получить подписки по userId
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
        
        // Создать новую подписку
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
        
        // Удалить подписку
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