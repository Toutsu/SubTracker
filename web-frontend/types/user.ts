// Типы для пользователей, основанные на shared/src/commonMain/User.kt
export interface User {
  id: string;
  username: string;
  email: string;
  passwordHash: string;
}