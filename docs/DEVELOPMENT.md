# Разработка SubTracker

## 📋 Предварительные требования

1. **Java 17+** - для сборки и запуска проекта
2. **Python 3.8+** - для Telegram бота
3. **Node.js 16+** - для веб-фронтенда
4. **Maven 3.8+** - для сборки бэкенда
5. **Git** - для клонирования репозитория

## 🔧 Быстрый старт

### 1. Клонирование репозитория

```bash
git clone <url_репозитория>
cd SubTracker
```

### 2. Запуск Backend (API сервер)

```bash
# Переход в директорию backend
cd backend

# Сборка и запуск backend
mvn spring-boot:run
```

Backend будет доступен по адресу: **http://localhost:8080**

**Что происходит при первом запуске:**
- ✅ Создается SQLite база данных `subtracker.db`
- ✅ Создаются необходимые таблицы (users, subscriptions)
- ✅ Добавляется тестовый пользователь: **user/user**
- ✅ Запускается API сервер с CORS поддержкой

### 3. Запуск Web Frontend

**Откройте новый терминал** и выполните:

```bash
# Переход в директорию веб-фронтенда
cd web-frontend

# Установка зависимостей
npm install

# Запуск в режиме разработки
npm run dev
```

Web-интерфейс будет доступен по адресу: **http://localhost:3000**

### 4. Запуск Telegram Bot

**Откройте новый терминал** и выполните:

```bash
# Переход в директорию Telegram бота
cd telegram-bot

# Создание виртуального окружения (опционально, но рекомендуется)
python -m venv venv
source venv/bin/activate  # На Windows: venv\Scripts\activate

# Установка зависимостей
pip install -r requirements.txt

# Создание .env файла с токеном бота
echo "TELEGRAM_BOT_TOKEN=your_bot_token_here" > .env

# Запуск бота
python bot.py
```

## 🌐 Веб-интерфейс

### Страницы приложения:

- **http://localhost:3000/** - страница входа (корневая)
- **http://localhost:3000/register** - регистрация новых пользователей
- **http://localhost:3000/dashboard** - главная страница после входа
- **http://localhost:3000/dashboard/subscriptions** - управление подписками

### 👤 Тестовые данные:

**Готовый тестовый аккаунт:**
- Логин: `user`
- Пароль: `user`

## 🧪 Тестирование

### Запуск тестов

```bash
# Backend тесты
cd backend
mvn test

# Тесты для веб-фронтенда запускаются через npm
cd web-frontend
npm test

# Тесты для Telegram бота
cd telegram-bot
python -m pytest
```

### Backend Tests

#### UserRepositoryTest.kt
```kotlin
class UserRepositoryTest {
    @Test
    fun `should create user successfully`()
    @Test
    fun `should not create duplicate user`()
    @Test
    fun `should authenticate user correctly`()
}
```

#### SubscriptionRepositoryTest.kt
```kotlin
class SubscriptionRepositoryTest {
    @Test
    fun `should add subscription successfully`()
    @Test
    fun `should get subscriptions by user id`()
    @Test
    fun `should delete subscription`()
    @Test
    fun `should update subscription`()
}
```

### Frontend Tests

#### Jest Tests
```bash
# Запуск всех тестов
npm test

# Запуск тестов в режиме watch
npm test:watch

# Запуск тестов с coverage
npm test:coverage
```

### Telegram Bot Tests

```bash
# Запуск всех тестов
python -m pytest

# Запуск тестов с coverage
python -m pytest --cov=.
```

### Принципы тестирования

**Arrange-Act-Assert pattern:**
```kotlin
@Test
fun `should create user successfully`() {
    // Arrange
    val username = "testuser"
    val email = "test@example.com"
    val password = "password123"
    
    // Act
    val result = userRepository.createUser(username, email, password)
    
    // Assert
    assertNotNull(result)
    assertEquals(username, result.username)
    assertEquals(email, result.email)
}
```

## 🐛 Troubleshooting

### Backend не запускается
```bash
# Очистка и пересборка
cd backend
mvn clean
mvn install
mvn spring-boot:run
```

### Ошибка "Address already in use"
```bash
# Найти и завершить процесс на порту 80
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Или использовать другой порт в application.properties
```

### CORS ошибки в браузере
- Убедитесь, что backend запущен на порту 8080
- Проверьте, что веб-сервер запущен на порту 3000
- CORS настроен для localhost:3000

### Web-сервер не запускается
```bash
# Альтернативные способы запуска HTTP сервера:

# Node.js (если установлен)
npx http-server -p 3000

# Python 3
python3 -m http.server 3000

# PHP (если установлен)
php -S localhost:3000
```

### Telegram Bot не запускается
```bash
# Проверьте, что установлены все зависимости
pip install -r requirements.txt

# Проверьте, что установлен токен бота
echo $TELEGRAM_BOT_TOKEN

# Проверьте, что backend API доступен
curl http://localhost:8080/health
```

## 🎯 Что можно делать

### После запуска вы можете:

1. **Войти в систему** через веб-интерфейс (user/user)
2. **Зарегистрировать новых пользователей**
3. **Тестировать API** через curl или Postman
4. **Разрабатывать новую функциональность**
5. **Добавлять новые страницы** в web-frontend
6. **Расширять API** в backend
7. **Добавлять новые команды** в Telegram бот

## 📝 Полезные команды

```bash
# Сборка backend
cd backend && mvn clean install

# Запуск backend
cd backend && mvn spring-boot:run

# Запуск тестов backend
cd backend && mvn test

# Установка зависимостей для веб-фронтенда
cd web-frontend && npm install

# Запуск веб-фронтенда в режиме разработки
cd web-frontend && npm run dev

# Запуск тестов для веб-фронтенда
cd web-frontend && npm test

# Установка зависимостей для Telegram бота
cd telegram-bot && pip install -r requirements.txt

# Запуск Telegram бота
cd telegram-bot && python bot.py
```

## 🚀 Готово к разработке!

После выполнения этих шагов у вас будет полностью рабочая система:
- ✅ Backend API с базой данных
- ✅ Веб-интерфейс с аутентификацией  
- ✅ Тестовые данные для начала работы
- ✅ CORS настроен для локальной разработки
- ✅ Telegram бот для управления подписками

Можно начинать разработку новых функций! 🎉