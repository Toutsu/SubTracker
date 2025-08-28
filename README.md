# 📊 SubTracker - Система управления подписками

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Multiplatform](https://img.shields.io/badge/Multiplatform-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-087CFA?style=for-the-badge&logo=ktor&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

[![CI/CD Pipeline](https://github.com/username/SubTracker/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/username/SubTracker/actions/workflows/ci-cd.yml)

**SubTracker** - это современное Kotlin Multiplatform приложение для отслеживания подписок и управления расходами. Проект включает в себя backend API, веб-интерфейс и Telegram бота с единой кодовой базой.

## ✨ Возможности

- 📱 **Telegram бот** с интуитивным интерфейсом
- 🌐 **Веб-приложение** с современным UI и системой аутентификации
- 🔗 **REST API** с CORS поддержкой для интеграции
- 💾 **База данных SQLite** для простого развертывания
- 👤 **Система пользователей** с регистрацией и JWT авторизацией
- 🧪 **Покрытие тестами** основных компонентов
- 🚀 **Готовность к разработке** с тестовыми данными

## 🏗️ Архитектура проекта

```
SubTracker/
├── 📦 shared/              # Общие модели и бизнес-логика
├── 🖥️ backend/             # REST API (Ktor + SQLite/PostgreSQL)
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
- **Ktor** веб-фреймворк с CORS поддержкой
- **Exposed** ORM для работы с БД
- **SQLite** база данных для разработки
- **HikariCP** пул соединений
- **JWT** аутентификация пользователей
- RESTful API endpoints
- Система регистрации и авторизации

#### 🌐 **:web-frontend** - Веб-интерфейс
- **HTML/CSS/JS** статические страницы
- Современный responsive дизайн
- Система аутентификации (вход/регистрация)
- Интеграция с backend API через fetch
- Тестовый пользователь: user/user
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
- 🐍 **Python 3** (для веб-сервера)

### 1. Клонирование проекта

```bash
git clone https://github.com/your-username/SubTracker.git
cd SubTracker
```

### 2. Запуск Backend API

```bash
./gradlew :backend:run
# Доступен по адресу: http://localhost:8080
# ✅ Автоматически создает SQLite БД
# ✅ Добавляет тестового пользователя user/user
```

### 3. Запуск Web Frontend (новый терминал)

```bash
cd web-frontend/src/jsMain/resources
python -m http.server 3000
# Доступен по адресу: http://localhost:3000
```

### 4. Готово! 🎉

- Откройте **http://localhost:3000** в браузере
- Войдите с данными: **user/user**
- Или зарегистрируйте нового пользователя

### 🔑 Тестовые данные
- **Логин:** `user`
- **Пароль:** `user`
- **База данных:** SQLite (создается автоматически)
- **CORS:** настроен для localhost:3000

### 🤖 Telegram бот (опционально)

```bash
# 1. Создайте .env файл с токеном бота
echo "TELEGRAM_BOT_TOKEN=your_bot_token" > .env

# 2. Запустите бота
./gradlew :telegram-bot:run
```

### 🐳 Docker (альтернативный способ)

```bash
# Запуск всех сервисов через Docker Compose
docker-compose up -d

# Только backend
docker-compose up -d backend

# С Telegram ботом
docker-compose --profile with-bot up -d
```

**Доступ:**
- Backend: http://localhost:8080
- Frontend: http://localhost:3000

### 5. Дополнительная настройка

**Для продакшена можно переключиться на PostgreSQL:**
1. Установите PostgreSQL
2. Создайте базу данных и пользователя
3. Обновите переменные окружения в `.env`:
   ```bash
   DATABASE_DRIVER=org.postgresql.Driver
   DATABASE_URL=jdbc:postgresql://localhost:5432/subtracker
   DATABASE_USER=your_user
   DATABASE_PASSWORD=your_password
   ```

## 📚 API Документация

### Endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `POST` | `/login` | Аутентификация пользователя |
| `POST` | `/register` | Регистрация нового пользователя |
| `GET` | `/subscriptions` | Получить все подписки (требуется аутентификация) |
| `GET` | `/subscriptions/{userId}` | Получить подписки пользователя (требуется аутентификация) |
| `POST` | `/subscriptions` | Создать новую подписку (требуется аутентификация) |
| `PUT` | `/subscriptions/{id}` | Обновить подписку (требуется аутентификация) |
| `DELETE` | `/subscriptions/{id}` | Удалить подписку (требуется аутентификация) |

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
- **Ktor** - веб-фреймворк с CORS поддержкой
- **Exposed** - ORM для работы с БД
- **SQLite** - база данных (по умолчанию)
- **PostgreSQL** - поддержка для продакшена
- **HikariCP** - пул соединений
- **JWT** - аутентификация пользователей
- **JUnit 5** - тестирование

### Frontend
- **HTML/CSS/JavaScript** - статические страницы
- **Fetch API** - HTTP запросы к backend
- **LocalStorage** - хранение JWT токенов
- **Responsive Design** - адаптивный интерфейс

### Telegram Bot
- **Java Telegram Bot API** - интеграция с Telegram
- **Kotlinx.coroutines** - асинхронность
- **HTTP Client** - взаимодействие с API

### Общее
- **Kotlin Multiplatform** - общий код
- **Kotlinx.serialization** - сериализация
- **Kotlinx.coroutines** - корутины
- **Gradle** - сборка проекта

## 🔒 Безопасность

- 🔐 **Переменные окружения** - все секретные данные вынесены в environment variables
- 📁 **Файл .env** - локальные настройки не попадают в репозиторий
- 📝 **Пример конфигурации** - файл `env.example` показывает, что нужно настроить
- ⚠️ **Без хардкода** - никаких токенов и паролей в исходном коде

## 📖 Документация

- [⚡ Быстрый запуск](QUICK_START.md) - запуск в 3 команды
- [📋 Подробные инструкции](ЗАПУСК_ПРОЕКТА.md) - полное руководство
- [🚀 CI/CD настройка](.github/DEPLOYMENT.md) - автоматическая сборка и деплой
- [✅ История разработки](todo-list.md) - список задач
- [🔧 Пример настроек](env.example) - переменные окружения

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
- 💬 Telegram: [@Toutsu](https://t.me/toutsu)
- 🐙 GitHub: [@Toutsu](https://github.com/toutsu)

---

⭐ **Поставьте звездочку, если проект был полезен!**

## CI/CD Статус
[![CI/CD Pipeline](https://github.com/toutsu/SubTracker/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/toutsu/SubTracker/actions/workflows/ci-cd.yml)
