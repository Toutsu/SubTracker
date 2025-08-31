# Развертывание SubTracker

## 🚀 Развертывание и конфигурация

### Переменные окружения

#### Backend
```bash
# Database Configuration
DATABASE_DRIVER=org.sqlite.JDBC                    # SQLite для разработки
DATABASE_URL=jdbc:sqlite:subtracker.db             # Путь к БД
DATABASE_USER=                                     # Пустой для SQLite
DATABASE_PASSWORD=                                 # Пустой для SQLite

# PostgreSQL для продакшена
DATABASE_DRIVER=org.postgresql.Driver
DATABASE_URL=jdbc:postgresql://localhost:5432/subtracker
DATABASE_USER=subtracker_user
DATABASE_PASSWORD=secure_password

# JWT Configuration
JWT_SECRET=your_super_secret_key_change_in_production
JWT_ISSUER=SubTracker
JWT_AUDIENCE=users
JWT_REALM=SubTracker
```

#### Telegram Bot
```bash
TELEGRAM_BOT_TOKEN=your_bot_token_from_botfather
BACKEND_API_URL=http://localhost:8080             # URL backend API
```

#### Web Frontend
```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080    # URL backend API
```

### Docker Compose конфигурация

**Сервисы:**
1. **backend** - API сервер на порту 80
2. **frontend** - Next.js приложение на порту 3000
3. **telegram-bot** - Telegram бот (profile: with-bot)

**Volumes:**
- `./data:/app/data` - персистентное хранение SQLite БД
- `./web-frontend/.next:/app/.next:ro` - статические файлы Next.js

**Networks:**
- `subtracker-network` - внутренняя сеть для межсервисного взаимодействия

**Health Checks:**
```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 30s
```

### Локальная разработка

1. **Требования:**
   - Java 17+
   - Python 3.8+
   - Node.js 16+
   - Maven 3.8+

2. **Запуск Backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   # Доступен на http://localhost:8080
   ```

3. **Запуск Web Frontend:**
   ```bash
   cd web-frontend
   npm install
   npm run dev
   # Доступен на http://localhost:3000
   ```

4. **Запуск Telegram Bot:**
   ```bash
   cd telegram-bot
   pip install -r requirements.txt
   echo "TELEGRAM_BOT_TOKEN=your_token" > .env
   python bot.py
   ```

### Docker развертывание

1. **Все сервисы:**
   ```bash
   docker-compose up -d
   ```

2. **Только backend и frontend:**
   ```bash
   docker-compose up -d backend frontend
   ```

3. **С Telegram ботом:**
   ```bash
   docker-compose --profile with-bot up -d
   ```

### Продакшн конфигурация

1. **Настройка PostgreSQL:**
   ```bash
   # Создание базы данных
   createdb subtracker
   createuser subtracker_user
   
   # Обновление .env
   DATABASE_DRIVER=org.postgresql.Driver
   DATABASE_URL=jdbc:postgresql://localhost:5432/subtracker
   DATABASE_USER=subtracker_user
   DATABASE_PASSWORD=secure_password
   ```

2. **Безопасность:**
   ```bash
   # Генерация JWT секрета
   JWT_SECRET=$(openssl rand -hex 32)
   
   # Настройка CORS для продакшн домена
   # Обновить allowedOrigins в CorsConfig.kt
   ```

3. **Сборка и запуск в продакшене:**
   ```bash
   # Сборка backend
   cd backend
   mvn clean package
   
   # Сборка frontend
   cd web-frontend
   npm run build
   
   # Запуск приложений
   # Backend:
   java -jar backend/target/backend-*.jar
   
   # Frontend:
   npm start
   
   # Telegram bot:
   cd telegram-bot
   python bot.py
   ```

## 📊 Мониторинг и логирование

### Health Check
Backend предоставляет endpoint `/health` для мониторинга:
```json
{
    "status": "UP",
    "timestamp": 1703123456789,
    "version": "1.0.0",
    "database": "SQLite"
}
```

### Логирование
- **Backend:** Spring Boot встроенное логирование
- **Database:** JPA/Hibernate SQL логирование
- **Telegram Bot:** Console логирование с timestamps
- **Frontend:** Browser console для отладки

### Обработка ошибок

#### Backend
```kotlin
try {
    // Операция с БД
} catch (e: Exception) {
    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to e.message))
}
```

#### Frontend
```javascript
try {
    const response = await fetch(url, options);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
} catch (error) {
    showMessage('Ошибка соединения с сервером', 'error');
}
```

#### Telegram Bot
```python
try:
    # API вызов
except Exception as e:
    await message.answer(f"❌ Ошибка: {str(e)}")
```

## 🔄 Жизненный цикл разработки

### Code Quality Tools

#### Backend (Maven plugins)
```xml
<plugin>
    <groupId>com.github.ozsie</groupId>
    <artifactId>detekt-maven-plugin</artifactId>
    <version>1.23.4</version>
</plugin>
```

#### Frontend (npm scripts)
```bash
# Сборка веб-фронтенда
npm run build

# Запуск тестов
npm test

# Линтинг
npm run lint
```

#### Telegram Bot (pytest)
```bash
# Запуск тестов
python -m pytest

# Запуск тестов с coverage
python -m pytest --cov=.
```

### CI/CD Pipeline

Проект готов для интеграции с GitHub Actions:
- Автоматические тесты при каждом push
- Статический анализ кода
- Сборка Docker образов
- Деплой в staging/production окружения

Пример workflow для GitHub Actions:
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'adopt'
    
    - name: Set up Node.js
      uses: actions/setup-node@v2
      with:
        node-version: '16'
    
    - name: Set up Python
      uses: actions/setup-python@v2
      with:
        python-version: '3.8'
    
    - name: Build backend
      run: cd backend && mvn clean package
    
    - name: Test backend
      run: cd backend && mvn test
    
    - name: Install frontend dependencies
      run: cd web-frontend && npm install
    
    - name: Test frontend
      run: cd web-frontend && npm test
    
    - name: Install bot dependencies
      run: cd telegram-bot && pip install -r requirements.txt
    
    - name: Test bot
      run: cd telegram-bot && python -m pytest