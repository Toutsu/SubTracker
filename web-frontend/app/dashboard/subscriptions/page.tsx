'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Header from '../../../components/Header';
import SubscriptionList from '../../../components/SubscriptionList';
import AddSubscriptionModal from '../../../components/AddSubscriptionModal';
import { authService } from '../../../services/auth';
import { subscriptionService } from '../../../services/subscriptions';
import type { Subscription } from '../../../types/subscription';

export default function SubscriptionsPage() {
  const [userId, setUserId] = useState('');
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
    setLoading(false);
  }, [router]);

  const handleAddSubscription = () => {
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
  };

  const handleSubscriptionAdded = () => {
    // Перезагружаем список подписок после добавления
    // В этом компоненте это не нужно, так как список подписок находится в другом компоненте
    // Но мы оставляем эту функцию для консистентности
  };

  if (loading) {
    return <div>Загрузка...</div>;
  }

  return (
    <div className="container">
      <Header />
      
      <div className="page-header">
        <h1>Управление подписками</h1>
        <button className="btn btn-primary" onClick={handleAddSubscription}>
          Добавить подписку
        </button>
      </div>
      
      <SubscriptionList userId={userId} />
      
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