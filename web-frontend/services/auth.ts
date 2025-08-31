// Сервис для работы с аутентификацией
import { apiClient } from './api';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

export class AuthService {
  // Вход пользователя
  async login(request: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await apiClient.post<AuthResponse>('/login', request);
      if (response.success && response.token) {
        apiClient.setToken(response.token);
      }
      return response;
    } catch (error) {
      console.error('Ошибка входа:', error);
      throw error;
    }
  }

  // Регистрация нового пользователя
  async register(request: RegisterRequest): Promise<AuthResponse> {
    try {
      return await apiClient.post<AuthResponse>('/register', request);
    } catch (error) {
      console.error('Ошибка регистрации:', error);
      throw error;
    }
  }

  // Выход пользователя
  logout(): void {
    apiClient.setToken(null);
  }

  // Проверка авторизации
  isAuthenticated(): boolean {
    // В реальном приложении здесь может быть проверка срока действия токена
    return typeof window !== 'undefined' && !!localStorage.getItem('authToken');
  }
}

// Создаем экземпляр сервиса аутентификации
export const authService = new AuthService();