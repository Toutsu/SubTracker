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
VITE_API_BASE_URL=http://localhost:8080    # URL backend API
```

### Docker Compose конфигурация

**Сервисы:**
1. **backend** - API сервер на порту 80
2. **frontend** - Vite приложение на порту 3000
3. **telegram-bot** - Telegram бот (profile: with-bot)

**Volumes:**
- `./data:/app/data` - персистентное хранение SQLite БД

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
   - Python 3.11+
   - Node.js 20+
   - Maven 3.9+

2. **Запуск Backend:**
   ```bash
   cd backend
   ./mvnw spring-boot:run
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
   # Обновить allowedOriginPatterns в SecurityConfig.kt
   ```

3. **Сборка и запуск в продакшене:**
   ```bash
   # Сборка backend
   cd backend
   ./mvnw clean package
   
   # Сборка frontend
   cd web-frontend
   npm run build
   
   # Запуск приложений
   # Backend:
   java -jar backend/target/*.jar
   
   # Frontend:
   npx vite preview
   
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

Проект использует GitHub Actions для автоматизации CI/CD:

- Автоматические тесты при каждом push
- Статический анализ кода
- Сборка Docker образов
- Деплой в staging/production окружения

Pipeline состоит из следующих этапов:

1. **build-and-test** - Сборка и тестирование всех компонентов
2. **code-quality** - Проверка качества кода и уязвимостей
3. **docker-build** - Сборка Docker образов (только для main ветки)
4. **integration-tests** - Интеграционные тесты с реальной базой данных
5. **deploy** - Деплой в продакшен (только для main ветки)
6. **notify** - Уведомления о результатах деплоя

Workflow файл: `.github/workflows/ci-cd.yml`

### Мониторинг и алертинг

#### Health Checks
- Backend: `/health` endpoint
- Frontend: доступность статических файлов
- Telegram Bot: периодическая проверка соединения с API

#### Логирование
- Все сервисы логируют в stdout/stderr
- Для продакшена рекомендуется использовать централизованное логирование (ELK, Grafana Loki)

#### Метрики
- JVM метрики для backend (через Spring Boot Actuator)
- Custom метрики для бизнес-логики
- Метрики производительности API

### Backup и восстановление

#### База данных
```bash
# Создание бэкапа SQLite
sqlite3 subtracker.db ".backup subtracker.db.backup.$(date +%Y%m%d_%H%M%S)"

# Восстановление из бэкапа
cp subtracker.db.backup.latest subtracker.db
```

#### Конфигурации
- Все конфигурации хранятся в переменных окружения
- Используйте secrets manager для продакшена

### Rollback процедур

1. **Docker контейнеры:**
   ```bash
   # Остановка текущего контейнера
   docker stop subtracker-backend
   
   # Запуск предыдущей версии
   docker run -d --name subtracker-backend-old subtracker-backend:v1.0.0
   ```

2. **База данных:**
   ```bash
   # Восстановление из последнего бэкапа
   cp /backups/subtracker.db.backup.latest /app/data/subtracker.db
   ```

### Troubleshooting

#### Частые проблемы и их решения

1. **Ошибка подключения к базе данных:**
   - Проверьте переменные окружения DATABASE_*
   - Убедитесь, что PostgreSQL запущен и доступен

2. **Ошибка авторизации:**
   - Проверьте JWT_SECRET
   - Убедитесь, что токен не истек

3. **Проблемы с Telegram ботом:**
   - Проверьте TELEGRAM_BOT_TOKEN
   - Убедитесь, что бот имеет доступ к интернету

4. **Проблемы с фронтендом:**
   - Проверьте VITE_API_BASE_URL
   - Убедитесь, что backend доступен

### Security Best Practices

1. **Secrets Management:**
   - Никогда не храните секреты в коде
   - Используйте GitHub Secrets для CI/CD
   - Используйте secrets manager в продакшене

2. **Network Security:**
   - Ограничьте доступ к портам
   - Используйте HTTPS в продакшене
   - Настройте CORS правильно

3. **Application Security:**
   - Регулярно обновляйте зависимости
   - Используйте статический анализ кода
   - Проводите penetration testing
## 🐳 Docker конфигурация

Каждый модуль имеет свой Dockerfile для сборки образа:

### Frontend (Vite)
- **Dockerfile**: `web-frontend/Dockerfile`
- **Сервер**: Nginx для раздачи статических файлов
- **Build директория**: `/app/build`
- **Порт**: 80

### Backend
- **Dockerfile**: `backend/Dockerfile`
- **Сервер**: Встроенный сервер Spring Boot
- **Порт**: 8080

### Telegram Bot
- **Dockerfile**: `telegram-bot/Dockerfile`
- **Сервер**: Python приложение
- **Порт**: отсутствует (не веб-сервис)

### Docker Compose
Файл `docker-compose.yml` содержит конфигурацию всех сервисов:
- **backend** - API сервер
- **frontend** - Vite приложение
- **telegram-bot** - Telegram бот (опционально)

Volumes:
- `./data:/app/data` - персистентное хранение SQLite БД

Networks:
- `subtracker-network` - внутренняя сеть для межсервисного взаимодействия