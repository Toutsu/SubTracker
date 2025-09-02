import { useState } from "react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { authApi, RegisterRequest } from "../services/api";

interface RegisterPageProps {
  onRegister: (email: string, password: string, name: string) => void;
  onSwitchToLogin: () => void;
}

export function RegisterPage({ onRegister, onSwitchToLogin }: RegisterPageProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  const [loading, setLoading] = useState(false);
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    
    if (password !== confirmPassword) {
      setError("Пароли не совпадают");
      setLoading(false);
      return;
    }
    
    if (password.length < 6) {
      setError("Пароль должен содержать минимум 6 символов");
      setLoading(false);
      return;
    }

    try {
      // Создаем объект запроса в соответствии с API
      const registerRequest: RegisterRequest = {
        username: name,
        email,
        password
      };
      
      // Выполняем запрос к API
      const response = await authApi.register({ registerRequest });
      
      // Проверяем успешность ответа
      if (response.success) {
        // Сохраняем токен в localStorage, если он есть
        if (response.token) {
          localStorage.setItem("auth_token", response.token);
        }
        // Вызываем коллбэк с данными пользователя
        onRegister(email, password, name);
      } else {
        setError(response.message || "Ошибка регистрации");
      }
    } catch (err: any) {
      // Проверяем, есть ли у ошибки сообщение от сервера
      if (err && err.response && err.response.status === 400) {
        // Для ошибки 400 пытаемся получить сообщение от сервера
        try {
          const errorData = await err.response.json();
          setError(errorData.message || "Пользователь с таким именем или email уже существует");
        } catch (parseError) {
          setError("Пользователь с таким именем или email уже существует");
        }
      } else {
        setError("Ошибка подключения к серверу");
        console.error("Registration error:", err);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-muted/30 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle>Создать аккаунт</CardTitle>
          <CardDescription>
            Зарегистрируйтесь для управления своими подписками
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">Имя</Label>
              <Input
                id="name"
                type="text"
                placeholder="Ваше имя"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="your@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Пароль</Label>
              <Input
                id="password"
                type="password"
                placeholder="Минимум 6 символов"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Подтвердите пароль</Label>
              <Input
                id="confirmPassword"
                type="password"
                placeholder="Повторите пароль"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                required
              />
            </div>
            {error && (
              <div className="text-destructive text-sm">{error}</div>
            )}
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Регистрация..." : "Зарегистрироваться"}
            </Button>
          </form>
          <div className="mt-4 text-center">
            <Button
              variant="link"
              onClick={onSwitchToLogin}
              className="text-muted-foreground"
            >
              Уже есть аккаунт? Войти
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}