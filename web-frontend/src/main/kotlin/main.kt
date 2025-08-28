import kotlinx.coroutines.*

fun main() {
    // Простое веб-приложение для отображения списка подписок
    println("Web Frontend started!")
    
    val subscriptions = listOf(
        mapOf(
            "id" to "1",
            "userId" to "user123",
            "name" to "Netflix",
            "price" to 15.99,
            "currency" to "USD",
            "billingCycle" to "monthly",
            "nextPaymentDate" to "2024-01-15"
        ),
        mapOf(
            "id" to "2",
            "userId" to "user123",
            "name" to "Spotify",
            "price" to 9.99,
            "currency" to "USD",
            "billingCycle" to "monthly",
            "nextPaymentDate" to "2024-01-20"
        )
    )
    
    // Отображаем список подписок
    displaySubscriptions(subscriptions)
}

fun displaySubscriptions(subscriptions: List<Map<String, Any>>) {
    println("=== Список подписок ===")
    
    subscriptions.forEach { subscription ->
        println("Подписка: ${subscription["name"]}")
        println("  Цена: ${subscription["price"]} ${subscription["currency"]}")
        println("  Цикл оплаты: ${subscription["billingCycle"]}")
        println("  Следующая оплата: ${subscription["nextPaymentDate"]}")
        println("---")
    }
}