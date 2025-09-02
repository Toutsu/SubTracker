import { Configuration } from '../api/runtime';
import { AuthControllerApi, SubscriptionControllerApi } from '../api';
import { authMiddleware } from './authMiddleware';

// Создаем конфигурацию API с базовым URL и middleware для аутентификации
const configuration = new Configuration({
  basePath: 'http://localhost:8080',
  middleware: [authMiddleware]
});

// Создаем экземпляры API клиентов с предварительно настроенной конфигурацией
export const authApi = new AuthControllerApi(configuration);
export const subscriptionApi = new SubscriptionControllerApi(configuration);

// Экспортируем модели для удобства использования
export type { 
  AuthResponse, 
  LoginRequest, 
  RegisterRequest, 
  SubscriptionResponse, 
  CreateSubscriptionRequest 
} from '../api/models';

// Экспортируем функции для проверки типов
export { 
  instanceOfLoginRequest, 
  instanceOfRegisterRequest, 
  instanceOfCreateSubscriptionRequest 
} from '../api/models';