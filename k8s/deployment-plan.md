# План деплоя SubTracker в Kubernetes

## Компоненты приложения

1. **Backend** (Kotlin/Spring Boot)
   - Порт: 8080
   - Зависимости: PostgreSQL
   - Конфигурации:
     - DATABASE_DRIVER
     - DATABASE_URL
     - DATABASE_USER
     - DATABASE_PASSWORD
     - JWT_SECRET
     - JWT_ISSUER
     - JWT_AUDIENCE
     - JWT_REALM

2. **Web Frontend** (Next.js)
   - Порт: 3000
   - Зависимости: Backend API
   - Конфигурации:
     - NEXT_PUBLIC_API_BASE_URL

3. **Telegram Bot** (Python/aiogram)
   - Порт: 8081 (для healthcheck)
   - Зависимости: Backend API, Redis
   - Конфигурации:
     - TELEGRAM_BOT_TOKEN
     - BACKEND_API_URL

4. **PostgreSQL**
   - Порт: 5432
   - Без специфичных конфигураций (стандартный образ)

5. **Redis**
   - Порт: 6379
   - Без специфичных конфигураций (стандартный образ)

## Порядок деплоя

1. PostgreSQL
2. Redis
3. Backend
4. Web Frontend
5. Telegram Bot

## Манифесты Kubernetes

### 1. ConfigMap
- Хранит общие конфигурации для всех компонентов
- Не содержит секретов

### 2. Secret
- Хранит секреты (JWT_SECRET, TELEGRAM_BOT_TOKEN, и т.д.)
- Использует base64 кодирование

### 3. Deployments и Services

#### PostgreSQL
- Deployment с образом postgres:15
- Service для доступа к БД
- PersistentVolume для хранения данных

#### Redis
- Deployment с образом redis:7-alpine
- Service для доступа к Redis

#### Backend
- Deployment с образом subtracker-backend
- Service для доступа к API
- Environment variables из ConfigMap и Secret

#### Web Frontend
- Deployment с образом subtracker-frontend
- Service для доступа к веб-интерфейсу
- Environment variables из ConfigMap

#### Telegram Bot
- Deployment с образом subtracker-telegram-bot
- Service для healthcheck
- Environment variables из ConfigMap и Secret

### 4. Ingress
- Маршрутизация внешнего трафика к Web Frontend
- Настройка SSL (опционально)

### 5. Kustomization
- Объединяет все манифесты в одну конфигурацию
- Позволяет параметризовать деплой

## Переменные окружения

### Backend
```
DATABASE_DRIVER: org.postgresql.Driver
DATABASE_URL: jdbc:postgresql://postgresql:5432/subtracker
DATABASE_USER: subtracker_user
DATABASE_PASSWORD: secure_password
JWT_SECRET: your_super_secret_key_change_in_production
JWT_ISSUER: SubTracker
JWT_AUDIENCE: users
JWT_REALM: SubTracker
```

### Web Frontend
```
NEXT_PUBLIC_API_BASE_URL: http://backend:8080
```

### Telegram Bot
```
TELEGRAM_BOT_TOKEN: your_bot_token_from_botfather
BACKEND_API_URL: http://backend:8080
```

## Инструкции по деплою

1. Создать namespace для приложения:
   ```bash
   kubectl create namespace subtracker
   ```

2. Применить манифесты:
   ```bash
   kubectl apply -k k8s/ -n subtracker
   ```

3. Проверить статус подов:
   ```bash
   kubectl get pods -n subtracker
   ```

4. Проверить сервисы:
   ```bash
   kubectl get services -n subtracker
   ```

5. Проверить ingress:
   ```bash
   kubectl get ingress -n subtracker
   ```

6. Получить внешний IP (если используется):
   ```bash
   kubectl get ingress subtracker-frontend -n subtracker