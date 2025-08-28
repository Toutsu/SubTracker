import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.SimpleKeyboardButton
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import kotlinx.coroutines.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

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

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String
)

class SubTrackerBot {
    private val bot: TelegramBot
    private val apiBaseUrl = "http://localhost:8080"
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    private val userStates = mutableMapOf<Long, BotState>()
    private val tempSubscriptions = mutableMapOf<Long, MutableMap<String, String>>()
    private val userTokens = mutableMapOf<Long, String>() // chatId -> JWT token
    
    enum class BotState {
        NONE,
        ADDING_SUBSCRIPTION_NAME,
        ADDING_SUBSCRIPTION_PRICE,
        ADDING_SUBSCRIPTION_CURRENCY,
        ADDING_SUBSCRIPTION_CYCLE,
        ADDING_SUBSCRIPTION_DATE,
        LOGIN_USERNAME,
        LOGIN_PASSWORD
    }
    
    init {
        val botToken = System.getenv("TELEGRAM_BOT_TOKEN") 
            ?: throw IllegalStateException("TELEGRAM_BOT_TOKEN environment variable is required")
        bot = TelegramBot(botToken)
    }
    
    fun start() {
        println("Starting Telegram Bot...")
        
        bot.buildBehaviourWithLongPolling {
            println("Bot is running... Press Ctrl+C to stop")
            
            // Команда старта
            onCommand("start") { message ->
                handleStartCommand(message.chat.id)
            }
            
            // Команда помощи
            onCommand("help") { message ->
                handleHelpCommand(message.chat.id)
            }
            
            // Команда списка подписок
            onCommand("list") { message ->
                handleListCommand(message.chat.id)
            }
            
            // Команда добавления подписки
            onCommand("add") { message ->
                handleAddCommand(message.chat.id)
            }
            
            // Команда удаления подписки
            onCommand("delete") { message ->
                val args = message.content.text.split(" ")
                if (args.size < 2) {
                    sendTextMessage(message.chat.id, "❌ Укажите ID подписки.\nПример: /delete abc123")
                } else {
                    handleDeleteCommand(message.chat.id, args[1])
                }
            }
            
            // Команда статистики
            onCommand("stats") { message ->
                handleStatsCommand(message.chat.id)
            }
            
            // Команда входа
            onCommand("login") { message ->
                handleLoginCommand(message.chat.id)
            }
            
            // Обработка текстовых сообщений
            on<TextContent> { message ->
                val chatId = message.chat.id
                val userId = chatId.chatId
                val text = message.content.text
                
                when (userStates[userId]) {
                    BotState.LOGIN_USERNAME -> {
                        userStates[userId] = BotState.LOGIN_PASSWORD
                        tempSubscriptions[userId] = mutableMapOf("username" to text)
                        sendTextMessage(chatId, "🔑 Введите пароль:")
                    }
                    
                    BotState.LOGIN_PASSWORD -> {
                        val username = tempSubscriptions[userId]?.get("username") ?: ""
                        handleLogin(chatId, username, text)
                    }
                    
                    else -> handleTextInput(chatId, userId, text)
                }
            }
        }.join()
    }
    
    private suspend fun handleStartCommand(chatId: ChatId) {
        val welcomeMessage = """
            🎉 Добро пожаловать в SubTracker!
            
            Я помогу вам управлять подписками и контролировать расходы.
            
            Для начала работы выполните вход:
            🔐 /login - Войти в систему
            
            Или создайте аккаунт через веб-интерфейс.
        """.trimIndent()
        
        sendTextMessage(chatId, welcomeMessage)
    }
    
    private suspend fun handleHelpCommand(chatId: ChatId) {
        val helpMessage = """
            📖 Справка по командам:
            
            🔐 /login - Войти в систему
            📋 /list - Показать все ваши подписки
            ➕ /add - Добавить новую подписку
            🗑 /delete [id] - Удалить подписку по ID
            📊 /stats - Показать статистику расходов
            ❓ /help - Показать эту справку
        """.trimIndent()
        
        sendTextMessage(chatId, helpMessage)
    }
    
    private suspend fun handleListCommand(chatId: ChatId) {
        val token = userTokens[chatId.chatId] ?: run {
            sendTextMessage(chatId, "🔒 Вы не авторизованы. Используйте /login для входа.")
            return
        }
        
        try {
            val subscriptions = httpClient.get("$apiBaseUrl/subscriptions") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body<List<Subscription>>()
            
            if (subscriptions.isEmpty()) {
                sendTextMessage(chatId, "📭 У вас пока нет подписок.\n\nИспользуйте /add для добавления первой подписки.")
            } else {
                val message = buildString {
                    appendLine("📋 Ваши подписки:")
                    appendLine()
                    
                    subscriptions.forEach { sub ->
                        appendLine("🔸 ${sub.name}")
                        appendLine("   💰 ${sub.price} ${sub.currency}")
                        appendLine("   🔄 ${translateBillingCycle(sub.billingCycle)}")
                        appendLine("   📅 Следующая оплата: ${sub.nextPaymentDate}")
                        appendLine("   📝 ID: ${sub.id}")
                        appendLine()
                    }
                    
                    appendLine("Для удаления используйте: /delete [ID]")
                }
                
                sendTextMessage(chatId, message)
            }
        } catch (e: Exception) {
            sendTextMessage(chatId, "❌ Ошибка при загрузке подписок: ${e.message}")
        }
    }
    
    private suspend fun handleAddCommand(chatId: ChatId) {
        val token = userTokens[chatId.chatId] ?: run {
            sendTextMessage(chatId, "🔒 Вы не авторизованы. Используйте /login для входа.")
            return
        }
        
        userStates[chatId.chatId] = BotState.ADDING_SUBSCRIPTION_NAME
        tempSubscriptions[chatId.chatId] = mutableMapOf()
        
        sendTextMessage(chatId, "➕ Добавление новой подписки\n\n📝 Введите название подписки (например: Netflix, Spotify):")
    }
    
    private suspend fun handleDeleteCommand(chatId: ChatId, id: String) {
        val token = userTokens[chatId.chatId] ?: run {
            sendTextMessage(chatId, "🔒 Вы не авторизованы. Используйте /login для входа.")
            return
        }
        
        try {
            val response = httpClient.delete("$apiBaseUrl/subscriptions/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            if (response.status.isSuccess()) {
                sendTextMessage(chatId, "✅ Подписка успешно удалена!")
            } else {
                sendTextMessage(chatId, "❌ Подписка не найдена или уже удалена.")
            }
        } catch (e: Exception) {
            sendTextMessage(chatId, "❌ Ошибка при удалении подписки: ${e.message}")
        }
    }
    
    private suspend fun handleStatsCommand(chatId: ChatId) {
        val token = userTokens[chatId.chatId] ?: run {
            sendTextMessage(chatId, "🔒 Вы не авторизованы. Используйте /login для входа.")
            return
        }
        
        try {
            val subscriptions = httpClient.get("$apiBaseUrl/subscriptions") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body<List<Subscription>>()
            
            if (subscriptions.isEmpty()) {
                sendTextMessage(chatId, "📊 Статистика недоступна - у вас нет подписок.")
                return
            }
            
            val activeCount = subscriptions.count { it.isActive }
            val totalCost = subscriptions
                .filter { it.isActive }
                .sumOf { it.price.toDoubleOrNull() ?: 0.0 }
            
            val nextPayment = subscriptions
                .filter { it.isActive }
                .minByOrNull { it.nextPaymentDate }
            
            val message = buildString {
                appendLine("📊 Статистика подписок:")
                appendLine()
                appendLine("📈 Активных подписок: $activeCount")
                appendLine("💰 Общая стоимость: $%.2f USD".format(totalCost))
                
                if (nextPayment != null) {
                    appendLine("📅 Ближайшая оплата: ${nextPayment.nextPaymentDate}")
                    appendLine("   ${nextPayment.name} - ${nextPayment.price} ${nextPayment.currency}")
                }
            }
            
            sendTextMessage(chatId, message)
        } catch (e: Exception) {
            sendTextMessage(chatId, "❌ Ошибка при загрузке статистики: ${e.message}")
        }
    }
    
    private suspend fun handleLoginCommand(chatId: ChatId) {
        userStates[chatId.chatId] = BotState.LOGIN_USERNAME
        tempSubscriptions[chatId.chatId] = mutableMapOf()
        sendTextMessage(chatId, "🔐 Вход в систему\n\n👤 Введите имя пользователя:")
    }
    
    private suspend fun handleLogin(chatId: ChatId, username: String, password: String) {
        try {
            val response = httpClient.post("$apiBaseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(username, password))
            }
            
            if (response.status.isSuccess()) {
                val loginResponse = response.body<LoginResponse>()
                userTokens[chatId.chatId] = loginResponse.token
                userStates.remove(chatId.chatId)
                tempSubscriptions.remove(chatId.chatId)
                
                sendTextMessage(chatId, "✅ Успешный вход! Теперь вы можете управлять подписками.")
            } else {
                sendTextMessage(chatId, "❌ Неверное имя пользователя или пароль.")
            }
        } catch (e: Exception) {
            sendTextMessage(chatId, "❌ Ошибка при входе: ${e.message}")
        }
    }
    
    private suspend fun handleTextInput(chatId: ChatId, userId: Long, text: String) {
        val state = userStates[userId] ?: BotState.NONE
        val tempSub = tempSubscriptions[userId] ?: return
        
        when (state) {
            BotState.ADDING_SUBSCRIPTION_NAME -> {
                tempSub["name"] = text
                userStates[userId] = BotState.ADDING_SUBSCRIPTION_PRICE
                sendTextMessage(chatId, "💰 Введите цену подписки (например: 15.99):")
            }
            
            BotState.ADDING_SUBSCRIPTION_PRICE -> {
                if (text.toDoubleOrNull() == null) {
                    sendTextMessage(chatId, "❌ Неверный формат цены. Введите число (например: 15.99):")
                    return
                }
                tempSub["price"] = text
                userStates[userId] = BotState.ADDING_SUBSCRIPTION_CURRENCY
                sendTextMessage(chatId, "💱 Введите валюту (например: USD, EUR, RUB):")
            }
            
            BotState.ADDING_SUBSCRIPTION_CURRENCY -> {
                if (text.length != 3) {
                    sendTextMessage(chatId, "❌ Неверный формат валюты. Используйте 3-буквенный код (например: USD):")
                    return
                }
                tempSub["currency"] = text
                userStates[userId] = BotState.ADDING_SUBSCRIPTION_CYCLE
                sendTextMessage(chatId, "🔄 Введите цикл оплаты (например: monthly, yearly, weekly):")
            }
            
            BotState.ADDING_SUBSCRIPTION_CYCLE -> {
                if (text !in listOf("monthly", "yearly", "weekly")) {
                    sendTextMessage(chatId, "❌ Неверный цикл оплаты. Используйте: monthly, yearly или weekly.")
                    return
                }
                tempSub["billingCycle"] = text
                userStates[userId] = BotState.ADDING_SUBSCRIPTION_DATE
                sendTextMessage(chatId, "📅 Введите дату следующей оплаты в формате ГГГГ-ММ-ДД (например: 2024-12-25):")
            }
            
            BotState.ADDING_SUBSCRIPTION_DATE -> {
                // Простая проверка формата даты
                if (!text.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    sendTextMessage(chatId, "❌ Неверный формат даты. Используйте ГГГГ-ММ-ДД (например: 2024-12-25):")
                    return
                }
                tempSub["nextPaymentDate"] = text
                
                // Создаем подписку
                try {
                    val token = userTokens[userId] ?: throw Exception("Not authenticated")
                    val request = CreateSubscriptionRequest(
                        userId = userId.toString(),
                        name = tempSub["name"]!!,
                        price = tempSub["price"]!!,
                        currency = tempSub["currency"]!!,
                        billingCycle = tempSub["billingCycle"]!!,
                        nextPaymentDate = tempSub["nextPaymentDate"]!!
                    )
                    
                    val response = httpClient.post("$apiBaseUrl/subscriptions") {
                        contentType(ContentType.Application.Json)
                        header(HttpHeaders.Authorization, "Bearer $token")
                        setBody(request)
                    }
                    
                    if (response.status.isSuccess()) {
                        sendTextMessage(chatId, "✅ Подписка '${tempSub["name"]}' успешно добавлена!")
                    } else {
                        sendTextMessage(chatId, "❌ Ошибка при добавлении подписки: ${response.status.description}")
                    }
                    
                    // Очищаем состояние
                    userStates.remove(userId)
                    tempSubscriptions.remove(userId)
                    
                } catch (e: Exception) {
                    sendTextMessage(chatId, "❌ Ошибка при добавлении подписки: ${e.message}")
                }
            }
            
            else -> {
                sendTextMessage(chatId, "❓ Не понимаю команду. Используйте /help для справки.")
            }
        }
    }
    
    private fun translateBillingCycle(cycle: String): String {
        return when (cycle) {
            "monthly" -> "Ежемесячно"
            "yearly" -> "Ежегодно"
            "weekly" -> "Еженедельно"
            else -> cycle
        }
    }
}

fun main() {
    val bot = SubTrackerBot()
    bot.start()
}
