// Сервис для работы с подписками
import { apiClient } from './api';
import type { Subscription, CreateSubscriptionRequest } from '../types/subscription';

export class SubscriptionService {
  // Получение всех подписок пользователя
  async getAllSubscriptions(userId: string): Promise<Subscription[]> {
    try {
      return await apiClient.get<Subscription[]>(`/subscriptions?userId=${userId}`);
    } catch (error) {
      console.error('Ошибка получения подписок:', error);
      throw error;
    }
  }

  // Добавление новой подписки
  async addSubscription(request: CreateSubscriptionRequest): Promise<Subscription> {
    try {
      return await apiClient.post<Subscription>('/subscriptions', request);
    } catch (error) {
      console.error('Ошибка добавления подписки:', error);
      throw error;
    }
  }

  // Удаление подписки
  async deleteSubscription(id: string): Promise<void> {
    try {
      await apiClient.delete(`/subscriptions/${id}`);
    } catch (error) {
      console.error('Ошибка удаления подписки:', error);
      throw error;
    }
  }
}

// Создаем экземпляр сервиса подписок
export const subscriptionService = new SubscriptionService();