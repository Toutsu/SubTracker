import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    suspend fun getAllSubscriptions(userId: String): List<Subscription>
    suspend fun addSubscription(subscription: Subscription)
}