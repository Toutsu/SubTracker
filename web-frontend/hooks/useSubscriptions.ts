// Хук для работы с подписками
import { useState, useEffect } from 'react';
import { subscriptionService } from '../services/subscriptions';
import type { Subscription, CreateSubscriptionRequest } from '../types/subscription';

export const useSubscriptions = (userId: string) => {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (userId) {
      loadSubscriptions();
    }
  }, [userId]);

  const loadSubscriptions = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await subscriptionService.getAllSubscriptions(userId);
      setSubscriptions(data);
    } catch (err) {
      setError('Не удалось загрузить подписки');
      console.error('Ошибка загрузки подписок:', err);
    } finally {
      setLoading(false);
    }
  };

  const addSubscription = async (subscription: CreateSubscriptionRequest) => {
    try {
      const newSubscription = await subscriptionService.addSubscription(subscription);
      setSubscriptions(prev => [...prev, newSubscription]);
      return newSubscription;
    } catch (err) {
      setError('Не удалось добавить подписку');
      console.error('Ошибка добавления подписки:', err);
      throw err;
    }
  };

  const deleteSubscription = async (id: string) => {
    try {
      await subscriptionService.deleteSubscription(id);
      setSubscriptions(prev => prev.filter(sub => sub.id !== id));
    } catch (err) {
      setError('Не удалось удалить подписку');
      console.error('Ошибка удаления подписки:', err);
      throw err;
    }
  };

  return {
    subscriptions,
    loading,
    error,
    loadSubscriptions,
    addSubscription,
    deleteSubscription,
  };
};