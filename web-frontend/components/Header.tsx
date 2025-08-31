'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '../services/auth';

export default function Header() {
  const [username, setUsername] = useState('');
  const router = useRouter();

  useEffect(() => {
    // Получаем имя пользователя из localStorage
    const storedUsername = localStorage.getItem('username');
    if (storedUsername) {
      setUsername(storedUsername);
    }
  }, []);

  const handleLogout = () => {
    authService.logout();
    router.push('/login');
  };

  return (
    <div className="header">
      <h1>SubTracker</h1>
      <div className="user-info">
        <span>{username ? `Привет, ${username}!` : 'Загрузка...'}</span>
        <button className="btn secondary" onClick={handleLogout}>
          Выйти
        </button>
      </div>
    </div>
  );
}