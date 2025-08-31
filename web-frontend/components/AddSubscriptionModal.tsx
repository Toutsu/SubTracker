'use client';

import { useState } from 'react';
import { subscriptionService } from '../services/subscriptions';
import type { CreateSubscriptionRequest } from '../types/subscription';

interface AddSubscriptionModalProps {
  userId: string;
  onClose: () => void;
  onAdd: () => void;
}

export default function AddSubscriptionModal({ userId, onClose, onAdd }: AddSubscriptionModalProps) {
  const [name, setName] = useState('');
  const [price, setPrice] = useState('');
  const [currency, setCurrency] = useState('USD');
  const [billingCycle, setBillingCycle] = useState('monthly');
  const [nextPaymentDate, setNextPaymentDate] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const request: CreateSubscriptionRequest = {
        userId,
        name,
        price: parseFloat(price),
        currency,
        billingCycle: billingCycle as 'monthly' | 'yearly' | 'weekly',
        nextPaymentDate,
      };

      await subscriptionService.addSubscription(request);
      onAdd(); // Уведомляем родительский компонент об обновлении
      onClose(); // Закрываем модальное окно
    } catch (err) {
      setError('Не удалось добавить подписку');
      console.error('Ошибка добавления подписки:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal">
      <div className="modal-content">
        <div className="modal-header">
          <h2>Добавить подписку</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        
        {error && <div className="error">{error}</div>}
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Название</label>
            <input
              type="text"
              id="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="price">Цена</label>
            <input
              type="number"
              id="price"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              step="0.01"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="currency">Валюта</label>
            <select
              id="currency"
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
            >
              <option value="USD">USD</option>
              <option value="EUR">EUR</option>
              <option value="RUB">RUB</option>
            </select>
          </div>
          
          <div className="form-group">
            <label htmlFor="billingCycle">Цикл оплаты</label>
            <select
              id="billingCycle"
              value={billingCycle}
              onChange={(e) => setBillingCycle(e.target.value)}
            >
              <option value="monthly">Ежемесячно</option>
              <option value="yearly">Ежегодно</option>
              <option value="weekly">Еженедельно</option>
            </select>
          </div>
          
          <div className="form-group">
            <label htmlFor="nextPaymentDate">Дата следующей оплаты</label>
            <input
              type="date"
              id="nextPaymentDate"
              value={nextPaymentDate}
              onChange={(e) => setNextPaymentDate(e.target.value)}
              required
            />
          </div>
          
          <div className="form-actions">
            <button 
              type="button" 
              className="btn btn-secondary" 
              onClick={onClose}
              disabled={loading}
            >
              Отмена
            </button>
            <button 
              type="submit" 
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Добавление...' : 'Добавить'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}