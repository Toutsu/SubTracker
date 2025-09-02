# SubTracker

**SubTracker** - это современное приложение для отслеживания подписок и управления расходами. Проект включает в себя backend API, веб-интерфейс и Telegram бота.

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
### 🐳 Docker развертывание

Каждый модуль имеет свой Dockerfile для сборки образа:

- **Frontend**: `web-frontend/Dockerfile` (Nginx сервер)
- **Backend**: `backend/Dockerfile` (Spring Boot приложение)
- **Telegram Bot**: `telegram-bot/Dockerfile` (Python приложение)

#### Команды Docker Compose:

```bash
# Сборка и запуск всех сервисов
docker-compose up -d --build

# Остановка всех сервисов
docker-compose down

# Просмотр логов
docker-compose logs -f
```
SubTracker/
├── 🖥️ backend/             # REST API (Kotlin + Spring Boot 3 + SQLite/PostgreSQL)
├── 🌐 web-frontend/        # Веб-интерфейс (Next.js + TypeScript)
└── 🤖 telegram-bot/        # Telegram бот (Python + aiogram)
```

## 🚀 Быстрый старт

### Предварительные требования

- ☕ **Java 17+**
- 🐍 **Python 3.8+**
- 🟦 **Node.js 16+**
- 📦 **Maven 3.8+**

### 1. Клонирование проекта

```bash
git clone https://github.com/your-username/SubTracker.git
cd SubTracker
```

### 2. Запуск Backend API

```bash
cd backend
mvn spring-boot:run
# Доступен по адресу: http://localhost:8080
# ✅ Автоматически создает SQLite БД
# ✅ Добавляет тестового пользователя user/user
```

### 3. Запуск Web Frontend (новый терминал)

```bash
cd web-frontend
npm install
npm run dev
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
# 1. Перейдите в директорию бота
cd telegram-bot

# 2. Создайте .env файл с токеном бота или скопируйте пример
cp .env.example .env
# Отредактируйте .env и добавьте ваш TELEGRAM_BOT_TOKEN

# 3. Установите зависимости
pip install -r requirements.txt

# 4. Запустите бота
python run_bot.py
```

### 🧪 Тестирование Telegram бота

```bash
# 1. Установите зависимости для тестирования
pip install -r requirements-test.txt

# 2. Запустите тесты
python run_tests.py

# Или напрямую через pytest
pytest tests/ -v
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

## 📚 Документация

Подробная документация доступна в следующих файлах:

- [Архитектура проекта](docs/ARCHITECTURE.md) - описание архитектуры системы
- [Разработка](docs/DEVELOPMENT.md) - инструкции по разработке и тестированию
- [API](docs/API.md) - документация по API endpoints
- [Развертывание](docs/DEPLOYMENT.md) - инструкции по развертыванию приложения
- [История изменений](CHANGELOG.md) - история изменений проекта

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
