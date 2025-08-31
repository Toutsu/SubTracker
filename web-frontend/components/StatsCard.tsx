'use client';

import type { Subscription } from '../types/subscription';

interface StatsCardProps {
  subscriptions: Subscription[];
}

export default function StatsCard({ subscriptions }: StatsCardProps) {
  // Подсчет активных подписок
  const activeCount = subscriptions.filter(sub => sub.isActive).length;
  
  // Подсчет общей стоимости активных подписок
  const totalCost = subscriptions
    .filter(sub => sub.isActive)
    .reduce((sum, sub) => sum + sub.price, 0);
  
  // Нахождение ближайшей даты оплаты
  const nextPayment = subscriptions
    .filter(sub => sub.isActive)
    .sort((a, b) => new Date(a.nextPaymentDate).getTime() - new Date(b.nextPaymentDate).getTime())[0];

  return (
    <div className="stats-card">
      <h2>Статистика</h2>
      <div className="stats-grid">
        <div className="stat-item">
          <span className="stat-label">Активные подписки:</span>
          <span className="stat-value">{activeCount}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Общая стоимость:</span>
          <span className="stat-value">${totalCost.toFixed(2)}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">Следующая оплата:</span>
          <span className="stat-value">
            {nextPayment ? new Date(nextPayment.nextPaymentDate).toLocaleDateString('ru-RU') : 'Нет данных'}
          </span>
        </div>
      </div>
    </div>
  );
}