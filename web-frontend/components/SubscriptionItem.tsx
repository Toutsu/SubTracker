'use client';

import type { Subscription } from '../types/subscription';

interface SubscriptionItemProps {
  subscription: Subscription;
  onDelete: (id: string) => void;
}

export default function SubscriptionItem({ subscription, onDelete }: SubscriptionItemProps) {
  // Функция для перевода цикла оплаты
  const translateBillingCycle = (cycle: string): string => {
    switch (cycle) {
      case 'monthly':
        return 'Ежемесячно';
      case 'yearly':
        return 'Ежегодно';
      case 'weekly':
        return 'Еженедельно';
      default:
        return cycle;
    }
  };

  // Функция для форматирования даты
  const formatDate = (dateString: string): string => {
    // В реальном приложении здесь будет более сложное форматирование
    return dateString;
  };

  return (
    <div className="subscription">
      <div className="subscription-header">
        <h3>{subscription.name}</h3>
        <button 
          className="btn btn-danger btn-sm" 
          onClick={() => onDelete(subscription.id)}
        >
          Удалить
        </button>
      </div>
      <p><strong>Цена:</strong> <span className="price">{subscription.price} {subscription.currency}</span></p>
      <p><strong>Цикл оплаты:</strong> {translateBillingCycle(subscription.billingCycle)}</p>
      <p><strong>Следующая оплата:</strong> {formatDate(subscription.nextPaymentDate)}</p>
      <p><strong>Статус:</strong> 
        <span style={{ color: subscription.isActive ? 'green' : 'red' }}>
          {subscription.isActive ? 'Активна' : 'Неактивна'}
        </span>
      </p>
    </div>
  );
}