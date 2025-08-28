# 📊 SubTracker - Система управления подписками

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Multiplatform](https://img.shields.io/badge/Multiplatform-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-087CFA?style=for-the-badge&logo=ktor&logoColor=white)

**SubTracker** - это современное Kotlin Multiplatform приложение для отслеживания подписок и управления расходами. Проект включает в себя backend API, веб-интерфейс и Telegram бота с единой кодовой базой.

## ✨ Возможности

- 📱 **Telegram бот** с интуитивным интерфейсом
- 🌐 **Веб-приложение** с современным UI
- 🔗 **REST API** для интеграции с другими сервисами
- 💾 **База данных PostgreSQL** для надежного хранения данных
- 🧪 **Полное покрытие тестами**
- 🚀 **Готовность к продакшену**

## 🏗️ Архитектура проекта

```
SubTracker/
├── 📦 shared/              # Общие модели и бизнес-логика
├── 🖥️ backend/             # REST API (Ktor + PostgreSQL)
├── 🌐 web-frontend/        # Веб-интерфейс (Kotlin/JS)
├── 🤖 telegram-bot/        # Telegram бот
└── 🧪 tests/               # Тесты для всех модулей
```

### 🔧 Модули

#### 📦 **:shared** - Общий код
- Модели данных (`User`, `Subscription`)
- Интерфейсы репозиториев
- Enum'ы и общие утилиты
- Сериализация Kotlinx

#### 🖥️ **:backend** - API сервер
- **Ktor** веб-фреймворк
- **Exposed** ORM для работы с БД
- **PostgreSQL** основная база данных
- **HikariCP** пул соединений
- RESTful API endpoints
- Обработка ошибок и валидация

#### 🌐 **:web-frontend** - Веб-интерфейс
- **Kotlin/JS** для фронтенда
- Современный responsive дизайн
- Модальные окна и уведомления
- Интеграция с backend API
- Статистика и аналитика

#### 🤖 **:telegram-bot** - Telegram бот
- Интуитивное меню с кнопками
- Пошаговое добавление подписок
- Статистика и управление
- Интеграция с backend API
- Обработка ошибок

## 🚀 Быстрый старт

### Предварительные требования

- ☕ **Java 17+**
- 🐘 **PostgreSQL 13+**
- 📦 **Node.js 16+** (для web-frontend)

### 1. Клонирование и сборка

```bash
git clone https://github.com/your-username/SubTracker.git
cd SubTracker
./gradlew build
```

### 2. Настройка базы данных

```sql
CREATE DATABASE subtracker;
CREATE USER subtracker_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE subtracker TO subtracker_user;
```

### 3. Настройка переменных окружения

```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/subtracker"
export DATABASE_USER="postgres"
export DATABASE_PASSWORD="password"
export TELEGRAM_BOT_TOKEN="your_bot_token"
```

### 4. Запуск сервисов

```bash
# Backend API (порт 8080)
./gradlew :backend:run

# Web-интерфейс (порт 3000)
./gradlew :web-frontend:browserDevelopmentRun

# Telegram бот
./gradlew :telegram-bot:run
```

## 📚 API Документация

### Endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/subscriptions` | Получить все подписки |
| `GET` | `/subscriptions/{userId}` | Получить подписки пользователя |
| `POST` | `/subscriptions` | Создать новую подписку |
| `PUT` | `/subscriptions/{id}` | Обновить подписку |
| `DELETE` | `/subscriptions/{id}` | Удалить подписку |

### Пример запроса

```json
POST /subscriptions
{
  "userId": "user123",
  "name": "Netflix",
  "price": "15.99",
  "currency": "USD",
  "billingCycle": "monthly",
  "nextPaymentDate": "2024-12-25"
}
```

## 🧪 Тестирование

```bash
# Все тесты
./gradlew test

# Конкретный модуль
./gradlew :backend:test
./gradlew :web-frontend:test
./gradlew :shared:test
```

## 🛠️ Технологический стек

### Backend
- **Kotlin/JVM** - основной язык
- **Ktor** - веб-фреймворк
- **Exposed** - ORM
- **PostgreSQL** - база данных
- **HikariCP** - пул соединений
- **JUnit 5** - тестирование

### Frontend
- **Kotlin/JS** - клиентский код
- **HTML5/CSS3** - интерфейс
- **Kotlinx.serialization** - JSON
- **Fetch API** - HTTP запросы

### Telegram Bot
- **Java Telegram Bot API** - интеграция с Telegram
- **Kotlinx.coroutines** - асинхронность
- **HTTP Client** - взаимодействие с API

### Общее
- **Kotlin Multiplatform** - общий код
- **Kotlinx.serialization** - сериализация
- **Kotlinx.coroutines** - корутины
- **Gradle** - сборка проекта

## 📖 Подробная документация

- [📋 Инструкции по запуску](ЗАПУСК_ПРОЕКТА.md)
- [✅ История разработки](todo-list.md)

## 🤝 Участие в разработке

1. Форкните репозиторий
2. Создайте ветку для новой функции (`git checkout -b feature/amazing-feature`)
3. Зафиксируйте изменения (`git commit -m 'Add amazing feature'`)
4. Отправьте в ветку (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект распространяется под лицензией MIT. См. файл [LICENSE](LICENSE) для подробностей.

## 📞 Контакты

- 📧 Email: hegin4@yandex.ru
- 💬 Telegram: [@Toutsu](https://t.me/yourusername)
- 🐙 GitHub: [@Toutsu](https://github.com/yourusername)

---

⭐ **Поставьте звездочку, если проект был полезен!**