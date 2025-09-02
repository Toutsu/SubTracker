import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { authApi, LoginRequest } from "../services/api";

interface LoginPageProps {
  onLogin: (email: string, password: string) => void;
  onSwitchToRegister: () => void;
}

export function LoginPage({ onLogin, onSwitchToRegister }: LoginPageProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    
    try {
      // Создаем объект запроса в соответствии с API
      const loginRequest: LoginRequest = {
        username,
        password
      };
      
      // Выполняем запрос к API
      const response = await authApi.login({ loginRequest });
      
      // Проверяем успешность ответа
      if (response.success) {
        // Сохраняем токен в localStorage, если он есть
        if (response.token) {
          localStorage.setItem("auth_token", response.token);
        }
        // Вызываем коллбэк с данными пользователя
        onLogin(username, password);
      } else {
        setError(response.message || "Ошибка входа");
      }
    } catch (err: any) {
      // Проверяем, есть ли у ошибки сообщение от сервера
      if (err && err.response && err.response.status === 400) {
        // Для ошибки 400 пытаемся получить сообщение от сервера
        try {
          const errorData = await err.response.json();
          setError(errorData.message || "Неверное имя пользователя или пароль");
        } catch (parseError) {
          setError("Неверное имя пользователя или пароль");
        }
      } else {
        setError("Ошибка подключения к серверу");
        console.error("Login error:", err);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/30 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle>Войти в аккаунт</CardTitle>
          <CardDescription>
            Войдите в свой аккаунт для управления подписками
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">Имя пользователя</Label>
              <Input
                id="username"
                type="text"
                placeholder="Введите имя пользователя"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Пароль</Label>
              <Input
                id="password"
                type="password"
                placeholder="Введите пароль"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            {error && (
              <div className="text-destructive text-sm">{error}</div>
            )}
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Вход..." : "Войти"}
            </Button>
          </form>
          <div className="mt-4 text-center">
            <Button
              variant="link"
              onClick={onSwitchToRegister}
              className="text-muted-foreground"
            >
              Нет аккаунта? Зарегистрироваться
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}