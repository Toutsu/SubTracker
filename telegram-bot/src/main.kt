import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.SendResponse
import kotlinx.coroutines.*
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI
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

class SubTrackerBot {
    private val bot: TelegramBot
    private val apiBaseUrl = "http://localhost:8080"
    private val httpClient = HttpClient.newHttpClient()
    private val userStates = mutableMapOf<Long, BotState>()
    private val tempSubscriptions = mutableMapOf<Long, MutableMap<String, String>>()
    
    enum class BotState {
        NONE,
        ADDING_SUBSCRIPTION_NAME,
        ADDING_SUBSCRIPTION_PRICE,
        ADDING_SUBSCRIPTION_CURRENCY,
        ADDING_SUBSCRIPTION_CYCLE,
        ADDING_SUBSCRIPTION_DATE
    }
    
    init {
        val botToken = System.getenv("TELEGRAM_BOT_TOKEN") 
            ?: "8368197859:AAHAlcm_UKKNaZ-qxZY72hOVmCFJNS_HIRg"
        bot = TelegramBot(botToken)
    }
    
    fun start() {
        println("Starting Telegram Bot...")
        
        bot.setUpdatesListener { updates ->
            for (update in updates) {
                try {
                    handleUpdate(update)
                } catch (e: Exception) {
                    println("Error handling update: ${e.message}")
                    e.printStackTrace()
                }
            }
            UpdatesListener.CONFIRMED_UPDATES_ALL
        }
        
        println("Bot is running... Press Ctrl+C to stop")
        
        // Добавляем shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Stopping bot...")
            bot.shutdown()
        })
        
        // Держим программу запущенной
        try {
            Thread.currentThread().join()
        } catch (e: InterruptedException) {
            println("Bot stopped")
        }
    }
    
    private fun handleUpdate(update: Update) {
        val message = update.message()
        if (message != null && message.text() != null) {
            val text = message.text()
            val chatId = message.chat().id()
            val userId = message.from().id()
            
            when {
                text.startsWith("/start") -> handleStartCommand(chatId, userId)
                text.startsWith("/help") -> handleHelpCommand(chatId)
                text.startsWith("/list") -> handleListCommand(chatId, userId)
                text.startsWith("/add") -> handleAddCommand(chatId, userId)
                text.startsWith("/delete") -> handleDeleteCommand(chatId, text)
                text.startsWith("/stats") -> handleStatsCommand(chatId, userId)
                text == "📋 Мои подписки" -> handleListCommand(chatId, userId)
                text == "➕ Добавить подписку" -> handleAddCommand(chatId, userId)
                text == "📊 Статистика" -> handleStatsCommand(chatId, userId)
                text == "❌ Отмена" -> handleCancelCommand(chatId, userId)
                else -> handleTextInput(chatId, userId, text)
            }
        }
    }
    
    private fun handleStartCommand(chatId: Long, userId: Long) {
        val welcomeMessage = """
            🎉 Добро пожаловать в SubTracker!
            
            Я помогу вам управлять подписками и контролировать расходы.
            
            Доступные команды:
            📋 /list - Показать все подписки
            ➕ /add - Добавить новую подписку
            📊 /stats - Показать статистику
            ❓ /help - Справка
        """.trimIndent()
        
        val keyboard = ReplyKeyboardMarkup(
            arrayOf(KeyboardButton("📋 Мои подписки"), KeyboardButton("➕ Добавить подписку")),
            arrayOf(KeyboardButton("📊 Статистика"), KeyboardButton("❓ Справка"))
        ).resizeKeyboard(true)
        
        sendMessage(chatId, welcomeMessage, keyboard)
    }
    
    private fun handleHelpCommand(chatId: Long) {
        val helpMessage = """
            📖 Справка по командам:
            
            📋 /list - Показать все ваши подписки
            ➕ /add - Добавить новую подписку
            🗑 /delete [id] - Удалить подписку по ID
            📊 /stats - Показать статистику расходов
            ❓ /help - Показать эту справку
            
            Используйте кнопки меню для удобной навигации!
        """.trimIndent()
        
        sendMessage(chatId, helpMessage)
    }
    
    private fun handleListCommand(chatId: Long, userId: Long) {
        GlobalScope.launch {
            try {
                val subscriptions = fetchUserSubscriptions(userId.toString())
                
                if (subscriptions.isEmpty()) {
                    sendMessage(chatId, "📭 У вас пока нет подписок.\n\nИспользуйте /add или кнопку '➕ Добавить подписку' для добавления первой подписки.")
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
                    
                    sendMessage(chatId, message)
                }
            } catch (e: Exception) {
                sendMessage(chatId, "❌ Ошибка при загрузке подписок: ${e.message}")
            }
        }
    }
    
    private fun handleAddCommand(chatId: Long, userId: Long) {
        userStates[userId] = BotState.ADDING_SUBSCRIPTION_NAME
        tempSubscriptions[userId] = mutableMapOf()
        
        val cancelKeyboard = ReplyKeyboardMarkup(
            arrayOf(KeyboardButton("❌ Отмена"))
        ).resizeKeyboard(true)
        
        sendMessage(chatId, "➕ Добавление новой подписки\n\n📝 Введите название подписки (например: Netflix, Spotify):", cancelKeyboard)
    }
    
    private fun handleDeleteCommand(chatId: Long, text: String) {
        val parts = text.split(" ")
        if (parts.size < 2) {
            sendMessage(chatId, "❌ Укажите ID подписки.\nПример: /delete abc123")
            return
        }
        
        val subscriptionId = parts[1]
        
        GlobalScope.launch {
            try {
                val success = deleteSubscription(subscriptionId)
                if (success) {
                    sendMessage(chatId, "✅ Подписка успешно удалена!")
                } else {
                    sendMessage(chatId, "❌ Подписка не найдена или уже удалена.")
                }
            } catch (e: Exception) {
                sendMessage(chatId, "❌ Ошибка при удалении подписки: ${e.message}")
            }
        }
    }
    
    private fun handleStatsCommand(chatId: Long, userId: Long) {
        GlobalScope.launch {
            try {
                val subscriptions = fetchUserSubscriptions(userId.toString())
                
                if (subscriptions.isEmpty()) {
                    sendMessage(chatId, "📊 Статистика недоступна - у вас нет подписок.")
                    return@launch
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
                
                sendMessage(chatId, message)
            } catch (e: Exception) {
                sendMessage(chatId, "❌ Ошибка при загрузке статистики: ${e.message}")
            }
        }
    }
    
    private fun handleCancelCommand(chatId: Long, userId: Long) {
        userStates.remove(userId)
        tempSubscriptions.remove(userId)
        
        val mainKeyboard = ReplyKeyboardMarkup(
            arrayOf(KeyboardButton("📋 Мои подписки"), KeyboardButton("➕ Добавить подписку")),
            arrayOf(KeyboardButton("📊 Статистика"), KeyboardButton("❓ Справка"))
        ).resizeKeyboard(true)
        
        sendMessage(chatId, "❌ Операция отменена.", mainKeyboard)
    }
    
    private fun handleTextInput(chatId: Long, userId: Long, text: String) {
        val state = userStates[userId] ?: BotState.NONE
        val tempSub = tempSubscriptions[userId] ?: return
        
        when (state) {
            BotState.ADDING_SUBSCRIPTION_NAME -> {
                tempSub["name"] = text
                userStates[userId] = BotState.ADDING_SUBSCRIPTION_PRICE
                sendMessage(chatId, "💰 Введите цену подписки (например: 15.99):")
            }
            
            BotState.ADDING_SUBSCRIPTION_PRICE -> {
                if (text.toDoubleOrNull() == null) {
                    sendMessage(chatId, "❌ Неверный формат цены. Введите число (например: 15.99):")
                    return
                }
                tempSub["price"] = text
                userStates[userId] = BotState.ADDING_SUBSCRIPTION_CURRENCY
                
                val currencyKeyboard = ReplyKeyboardMarkup(
                    arrayOf(KeyboardButton("USD"), KeyboardButton("EUR"), KeyboardButton("RUB")),
                    arrayOf(KeyboardButton("❌ Отмена"))
                ).resizeKeyboard(true)
                
                sendMessage(chatId, "💱 Выберите валюту:", currencyKeyboard)
            }
            
            BotState.ADDING_SUBSCRIPTION_CURRENCY -> {
                if (text !in listOf("USD", "EUR", "RUB", "GBP")) {
                    sendMessage(chatId, "❌ Неподдерживаемая валюта. Выберите USD, EUR или RUB:")
                    return
                }
                tempSub["currency"] = text
                userStates[userId] = BotState.ADDING_SUBSCRIPTION_CYCLE
                
                val cycleKeyboard = ReplyKeyboardMarkup(
                    arrayOf(KeyboardButton("Ежемесячно"), KeyboardButton("Ежегодно")),
                    arrayOf(KeyboardButton("Еженедельно"), KeyboardButton("❌ Отмена"))
                ).resizeKeyboard(true)
                
                sendMessage(chatId, "🔄 Выберите цикл оплаты:", cycleKeyboard)
            }
            
            BotState.ADDING_SUBSCRIPTION_CYCLE -> {
                val cycle = when (text) {
                    "Ежемесячно" -> "monthly"
                    "Ежегодно" -> "yearly"
                    "Еженедельно" -> "weekly"
                    else -> {
                        sendMessage(chatId, "❌ Неверный цикл оплаты. Выберите из предложенных вариантов:")
                        return
                    }
                }
                tempSub["billingCycle"] = cycle
                userStates[userId] = BotState.ADDING_SUBSCRIPTION_DATE
                
                val cancelKeyboard = ReplyKeyboardMarkup(
                    arrayOf(KeyboardButton("❌ Отмена"))
                ).resizeKeyboard(true)
                
                sendMessage(chatId, "📅 Введите дату следующей оплаты в формате ГГГГ-ММ-ДД (например: 2024-12-25):", cancelKeyboard)
            }
            
            BotState.ADDING_SUBSCRIPTION_DATE -> {
                // Простая проверка формата даты
                if (!text.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                    sendMessage(chatId, "❌ Неверный формат даты. Используйте ГГГГ-ММ-ДД (например: 2024-12-25):")
                    return
                }
                tempSub["nextPaymentDate"] = text
                
                // Создаем подписку
                GlobalScope.launch {
                    try {
                        val request = CreateSubscriptionRequest(
                            userId = userId.toString(),
                            name = tempSub["name"]!!,
                            price = tempSub["price"]!!,
                            currency = tempSub["currency"]!!,
                            billingCycle = tempSub["billingCycle"]!!,
                            nextPaymentDate = tempSub["nextPaymentDate"]!!
                        )
                        
                        createSubscription(request)
                        
                        val mainKeyboard = ReplyKeyboardMarkup(
                            arrayOf(KeyboardButton("📋 Мои подписки"), KeyboardButton("➕ Добавить подписку")),
                            arrayOf(KeyboardButton("📊 Статистика"), KeyboardButton("❓ Справка"))
                        ).resizeKeyboard(true)
                        
                        sendMessage(chatId, "✅ Подписка '${tempSub["name"]}' успешно добавлена!", mainKeyboard)
                        
                        // Очищаем состояние
                        userStates.remove(userId)
                        tempSubscriptions.remove(userId)
                        
                    } catch (e: Exception) {
                        sendMessage(chatId, "❌ Ошибка при добавлении подписки: ${e.message}")
                    }
                }
            }
            
            else -> {
                sendMessage(chatId, "❓ Не понимаю команду. Используйте /help для справки.")
            }
        }
    }
    
    private suspend fun fetchUserSubscriptions(userId: String): List<Subscription> {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$apiBaseUrl/subscriptions/$userId"))
                .GET()
                .build()
            
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            
            if (response.statusCode() == 200) {
                Json.decodeFromString<List<Subscription>>(response.body())
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            println("Error fetching subscriptions: ${e.message}")
            emptyList()
        }
    }
    
    private suspend fun createSubscription(request: CreateSubscriptionRequest) {
        val json = Json.encodeToString(request)
        
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$apiBaseUrl/subscriptions"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build()
        
        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        
        if (response.statusCode() !in 200..299) {
            throw Exception("HTTP ${response.statusCode()}: ${response.body()}")
        }
    }
    
    private suspend fun deleteSubscription(id: String): Boolean {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$apiBaseUrl/subscriptions/$id"))
                .DELETE()
                .build()
            
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            response.statusCode() in 200..299
        } catch (e: Exception) {
            println("Error deleting subscription: ${e.message}")
            false
        }
    }
    
    private fun sendMessage(chatId: Long, text: String, keyboard: ReplyKeyboardMarkup? = null) {
        val request = SendMessage(chatId, text)
        keyboard?.let { request.replyMarkup(it) }
        
        try {
            val response: SendResponse = bot.execute(request)
            if (!response.isOk) {
                println("Failed to send message: ${response.description()}")
            }
        } catch (e: Exception) {
            println("Error sending message: ${e.message}")
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
