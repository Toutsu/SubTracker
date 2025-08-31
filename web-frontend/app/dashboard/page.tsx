'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Header from '../../components/Header';
import SubscriptionList from '../../components/SubscriptionList';
import StatsCard from '../../components/StatsCard';
import AddSubscriptionModal from '../../components/AddSubscriptionModal';
import { authService } from '../../services/auth';
import { subscriptionService } from '../../services/subscriptions';
import type { Subscription } from '../../types/subscription';

export default function DashboardPage() {
  const [userId, setUserId] = useState('');
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
 const [showModal, setShowModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // Проверяем авторизацию
    if (!authService.isAuthenticated()) {
      router.push('/login');
      return;
    }

    // В реальном приложении userId будет получаться из токена
    // Пока используем заглушку
    setUserId('current-user');
    loadSubscriptions();
  }, [router]);

  const loadSubscriptions = async () => {
    try {
      setLoading(true);
      const data = await subscriptionService.getAllSubscriptions(userId);
      setSubscriptions(data);
    } catch (err) {
      console.error('Ошибка загрузки подписок:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAddSubscription = () => {
    setShowModal(true);
 };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  const handleSubscriptionAdded = () => {
    // Перезагружаем список подписок после добавления
    loadSubscriptions();
  };

  if (loading) {
    return <div>Загрузка...</div>;
  }

  return (
    <div className="container">
      <Header />
      
      <div className="welcome-card">
        <h2>Добро пожаловать в SubTracker!</h2>
        <p>
          Здесь вы можете управлять своими подписками, отслеживать расходы и не пропускать важные платежи. 
          Система поможет вам контролировать все ваши регулярные подписки в одном месте.
        </p>
        <div className="actions">
          <button className="btn" onClick={handleAddSubscription}>
            Добавить подписку
          </button>
        </div>
      </div>
      
      <StatsCard subscriptions={subscriptions} />
      
      <div className="subscriptions-section">
        <div className="section-header">
          <h2>Мои подписки</h2>
        </div>
        <SubscriptionList userId={userId} />
      </div>
      
      {showModal && (
        <AddSubscriptionModal
          userId={userId}
          onClose={handleCloseModal}
          onAdd={handleSubscriptionAdded}
        />
      )}
    </div>
  );
}