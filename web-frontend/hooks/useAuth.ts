// Хук для работы с аутентификацией
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '../services/auth';
import type { User } from '../types/user';

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    checkAuthStatus();
  }, []);

  const checkAuthStatus = () => {
    setLoading(true);
    try {
      // В реальном приложении здесь будет проверка токена и получение данных пользователя
      // Пока используем заглушку
      if (authService.isAuthenticated()) {
        // Получаем данные пользователя из localStorage или API
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
          setUser(JSON.parse(storedUser));
        }
      }
    } catch (error) {
      console.error('Ошибка проверки авторизации:', error);
    } finally {
      setLoading(false);
    }
  };

  const login = async (username: string, password: string) => {
    try {
      const response = await authService.login({ username, password });
      if (response.success && response.user && response.token) {
        setUser(response.user);
        localStorage.setItem('user', JSON.stringify(response.user));
        return response;
      }
      throw new Error(response.message);
    } catch (error) {
      console.error('Ошибка входа:', error);
      throw error;
    }
  };

  const register = async (username: string, email: string, password: string) => {
    try {
      const response = await authService.register({ username, email, password });
      return response;
    } catch (error) {
      console.error('Ошибка регистрации:', error);
      throw error;
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
    localStorage.removeItem('user');
    router.push('/login');
 };

  return {
    user,
    loading,
    isAuthenticated: !!user,
    login,
    register,
    logout,
    checkAuthStatus,
  };
};