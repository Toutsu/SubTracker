# ⚡ Быстрый запуск SubTracker

## 🚀 Запуск в 3 команды

### 1. Backend API
```bash
./gradlew :backend:run
```
**Результат:** http://localhost:8080 (создается SQLite БД + тестовый пользователь user/user)

### 2. Web Frontend (новый терминал)
```bash
cd web-frontend/src/jsMain/resources && python -m http.server 3000
```
**Результат:** http://localhost:3000 (веб-интерфейс)

### 3. Готово! 
Открой http://localhost:3000 и войди как **user/user**

---

## 📋 Что доступно

### 🌐 Веб-страницы:
- **http://localhost:3000/** - вход в систему
- **http://localhost:3000/register.html** - регистрация  
- **http://localhost:3000/dashboard.html** - главная страница

### 🔌 API Endpoints:
- `POST /login` - вход (возвращает JWT токен)
- `POST /register` - регистрация нового пользователя
- `GET /subscriptions` - список подписок (требует токен)
- `POST /subscriptions` - создать подписку (требует токен)
- `DELETE /subscriptions/{id}` - удалить подписку (требует токен)

### 🧪 Тестирование API:
```bash
# Вход
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user"}'

# Получить подписки  
curl -X GET http://localhost:8080/subscriptions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🤖 Telegram Bot (опционально)

1. Создай файл `.env`:
   ```bash
   echo "TELEGRAM_BOT_TOKEN=your_bot_token" > .env
   ```

2. Запусти бота:
   ```bash
   ./gradlew :telegram-bot:run
   ```

---

## 🛠️ Полезные команды

```bash
# Сборка проекта
./gradlew build

# Очистка
./gradlew clean

# Тесты
./gradlew test

# Остановить все процессы
Ctrl+C в каждом терминале
```

---

## 📁 Файлы проекта

```
SubTracker/
├── subtracker.db              # SQLite база (создается автоматически)
├── backend/                   # API сервер (Ktor + Exposed)
├── web-frontend/src/jsMain/resources/  # HTML страницы
├── telegram-bot/              # Telegram бот
└── shared/                    # Общие модели данных
```

**Готово к разработке!** 🎉
