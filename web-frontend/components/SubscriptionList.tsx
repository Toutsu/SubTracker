'use client';

import { useState, useEffect } from 'react';
import { subscriptionService } from '../services/subscriptions';
import SubscriptionItem from './SubscriptionItem';
import type { Subscription } from '../types/subscription';

interface SubscriptionListProps {
  userId: string;
}

export default function SubscriptionList({ userId }: SubscriptionListProps) {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadSubscriptions();
  }, [userId]);

  const loadSubscriptions = async () => {
    try {
      setLoading(true);
      const data = await subscriptionService.getAllSubscriptions(userId);
      setSubscriptions(data);
    } catch (err) {
      setError('Не удалось загрузить подписки');
      console.error('Ошибка загрузки подписок:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await subscriptionService.deleteSubscription(id);
      // Перезагружаем список после удаления
      loadSubscriptions();
    } catch (err) {
      setError('Не удалось удалить подписку');
      console.error('Ошибка удаления подписки:', err);
    }
  };

  if (loading) {
    return <div>Загрузка подписок...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  if (subscriptions.length === 0) {
    return (
      <div className="no-subscriptions">
        <p>У вас пока нет подписок</p>
        <button className="btn btn-primary">Добавить первую подписку</button>
      </div>
    );
  }

  return (
    <div className="subscriptions-container">
      {subscriptions.map((subscription) => (
        <SubscriptionItem
          key={subscription.id}
          subscription={subscription}
          onDelete={handleDelete}
        />
      ))}
    </div>
  );
}