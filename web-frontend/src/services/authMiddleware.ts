import { Middleware, RequestContext, ResponseContext } from '../api/runtime';

// Middleware для добавления токена аутентификации в заголовки запросов
export const authMiddleware: Middleware = {
  pre: async (context: RequestContext) => {
    // Получаем токен из localStorage
    const token = localStorage.getItem('auth_token');
    
    // Если токен существует, добавляем его в заголовки
    if (token) {
      context.init.headers = {
        ...context.init.headers,
        'Authorization': `Bearer ${token}`
      };
    }
    
    return context;
  },
  
  // Обработка ответов для автоматического выхода при истечении срока действия токена
  post: async (context: ResponseContext) => {
    // Если получили 401 или 403, очищаем токен и перенаправляем на страницу входа
    if (context.response.status === 401 || context.response.status === 403) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('subscriptions_user');
      // Перезагружаем страницу для возврата к форме входа
      window.location.reload();
    }
    
    return context.response;
  }
};