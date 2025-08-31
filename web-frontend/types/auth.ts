// Типы для аутентификации, основанные на shared/src/commonMain/RegisterRequest.kt
export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  user?: User;
  token?: string;
}

// Импортируем User из соответствующего файла
import type { User } from './user';