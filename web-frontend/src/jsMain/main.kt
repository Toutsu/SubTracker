import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.w3c.dom.*
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise

@Serializable
data class Subscription(
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

val API_BASE_URL = "http://localhost:8080"

fun main() {
    console.log("SubTracker Web Frontend started!")
    
    // Ждем загрузки DOM
    document.addEventListener("DOMContentLoaded", {
        initializeApp()
    })
}

fun initializeApp() {
    setupEventListeners()
    loadSubscriptions()
}

fun setupEventListeners() {
    // Кнопка добавления подписки
    document.getElementById("add-subscription-btn")?.addEventListener("click", {
        showAddSubscriptionModal()
    })
    
    // Кнопка закрытия модального окна
    document.getElementById("close-modal")?.addEventListener("click", {
        hideAddSubscriptionModal()
    })
    
    // Форма добавления подписки
    document.getElementById("add-subscription-form")?.addEventListener("submit", { event ->
        event.preventDefault()
        addSubscription()
    })
}

fun showAddSubscriptionModal() {
    document.getElementById("add-subscription-modal")?.let { modal ->
        (modal as HTMLElement).style.display = "block"
    }
}

fun hideAddSubscriptionModal() {
    document.getElementById("add-subscription-modal")?.let { modal ->
        (modal as HTMLElement).style.display = "none"
    }
    // Очищаем форму
    (document.getElementById("add-subscription-form") as HTMLFormElement).reset()
}

fun loadSubscriptions() {
    GlobalScope.launch {
        try {
            val subscriptions = fetchSubscriptions()
            displaySubscriptions(subscriptions)
            updateStats(subscriptions)
        } catch (e: Exception) {
            console.error("Ошибка загрузки подписок:", e)
            showError("Не удалось загрузить подписки")
        }
    }
}

suspend fun fetchSubscriptions(): List<Subscription> {
    val response = window.fetch("$API_BASE_URL/subscriptions")
        .await()
    
    if (!response.ok) {
        throw Exception("HTTP error! status: ${response.status}")
    }
    
    val jsonText = response.text().await()
    return Json.decodeFromString<List<Subscription>>(jsonText)
}

fun addSubscription() {
    GlobalScope.launch {
        try {
            val form = document.getElementById("add-subscription-form") as HTMLFormElement
            val formData = FormData(form)
            
            val request = CreateSubscriptionRequest(
                userId = "user123", // В реальном приложении будет браться из сессии
                name = formData.get("name") as String,
                price = formData.get("price") as String,
                currency = formData.get("currency") as String,
                billingCycle = formData.get("billingCycle") as String,
                nextPaymentDate = formData.get("nextPaymentDate") as String
            )
            
            val response = window.fetch("$API_BASE_URL/subscriptions", RequestInit(
                method = "POST",
                headers = js("({'Content-Type': 'application/json'})"),
                body = Json.encodeToString(request)
            )).await()
            
            if (response.ok) {
                hideAddSubscriptionModal()
                loadSubscriptions() // Перезагружаем список
                showSuccess("Подписка успешно добавлена!")
            } else {
                throw Exception("Ошибка при добавлении подписки")
            }
        } catch (e: Exception) {
            console.error("Ошибка добавления подписки:", e)
            showError("Не удалось добавить подписку")
        }
    }
}

fun displaySubscriptions(subscriptions: List<Subscription>) {
    val container = document.getElementById("subscriptions-container")
    container?.innerHTML = ""
    
    if (subscriptions.isEmpty()) {
        container?.innerHTML = """
            <div class="no-subscriptions">
                <p>У вас пока нет подписок</p>
                <button onclick="showAddSubscriptionModal()" class="btn btn-primary">Добавить первую подписку</button>
            </div>
        """.trimIndent()
        return
    }
    
    subscriptions.forEach { subscription ->
        val subscriptionElement = createSubscriptionElement(subscription)
        container?.appendChild(subscriptionElement)
    }
}

fun createSubscriptionElement(subscription: Subscription): HTMLElement {
    val div = document.createElement("div") as HTMLDivElement
    div.className = "subscription"
    
    div.innerHTML = """
        <div class="subscription-header">
            <h3>${subscription.name}</h3>
            <button class="btn btn-danger btn-sm" onclick="deleteSubscription('${subscription.id}')">Удалить</button>
        </div>
        <p><strong>Цена:</strong> <span class="price">${subscription.price} ${subscription.currency}</span></p>
        <p><strong>Цикл оплаты:</strong> ${translateBillingCycle(subscription.billingCycle)}</p>
        <p><strong>Следующая оплата:</strong> ${formatDate(subscription.nextPaymentDate)}</p>
        <p><strong>Статус:</strong> <span style="color: ${if (subscription.isActive) "green" else "red"};">
            ${if (subscription.isActive) "Активна" else "Неактивна"}
        </span></p>
    """.trimIndent()
    
    return div
}

fun deleteSubscription(id: String) {
    if (!window.confirm("Вы уверены, что хотите удалить эту подписку?")) {
        return
    }
    
    GlobalScope.launch {
        try {
            val response = window.fetch("$API_BASE_URL/subscriptions/$id", RequestInit(
                method = "DELETE"
            )).await()
            
            if (response.ok) {
                loadSubscriptions() // Перезагружаем список
                showSuccess("Подписка удалена!")
            } else {
                throw Exception("Ошибка при удалении подписки")
            }
        } catch (e: Exception) {
            console.error("Ошибка удаления подписки:", e)
            showError("Не удалось удалить подписку")
        }
    }
}

fun updateStats(subscriptions: List<Subscription>) {
    val activeCount = subscriptions.count { it.isActive }
    val totalCost = subscriptions
        .filter { it.isActive }
        .sumOf { it.price.toDoubleOrNull() ?: 0.0 }
    
    document.getElementById("active-count")?.textContent = activeCount.toString()
    document.getElementById("total-cost")?.textContent = "$%.2f".format(totalCost)
    
    // Находим ближайшую дату оплаты
    val nextPayment = subscriptions
        .filter { it.isActive }
        .minByOrNull { it.nextPaymentDate }
    
    if (nextPayment != null) {
        document.getElementById("next-payment")?.textContent = formatDate(nextPayment.nextPaymentDate)
    }
}

fun translateBillingCycle(cycle: String): String {
    return when (cycle) {
        "monthly" -> "Ежемесячно"
        "yearly" -> "Ежегодно"
        "weekly" -> "Еженедельно"
        else -> cycle
    }
}

fun formatDate(dateString: String): String {
    // Простое форматирование даты
    return dateString
}

fun showSuccess(message: String) {
    showNotification(message, "success")
}

fun showError(message: String) {
    showNotification(message, "error")
}

fun showNotification(message: String, type: String) {
    val notification = document.createElement("div") as HTMLDivElement
    notification.className = "notification $type"
    notification.textContent = message
    
    document.body?.appendChild(notification)
    
    // Удаляем уведомление через 3 секунды
    window.setTimeout({
        notification.remove()
    }, 3000)
}

// Глобальные функции для использования в HTML
@JsName("showAddSubscriptionModal")
fun jsShowAddSubscriptionModal() = showAddSubscriptionModal()

@JsName("deleteSubscription")
fun jsDeleteSubscription(id: String) = deleteSubscription(id)