'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import LoginForm from '../components/LoginForm';
import { authService } from '../services/auth';

export default function Home() {
  const router = useRouter();

  useEffect(() => {
    // Проверяем, авторизован ли пользователь
    if (authService.isAuthenticated()) {
      router.push('/dashboard');
    }
  }, [router]);

  return (
    <div className="container">
      <LoginForm />
    </div>
  );
}